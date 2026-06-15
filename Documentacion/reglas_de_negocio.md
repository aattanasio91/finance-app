# Reglas de Negocio

## 1. Usuarios

### RN-001: Registro
- El email debe ser único en el sistema.
- La contraseña debe tener al menos 8 caracteres, una mayúscula y un número.
- Al registrarse, se crean las categorías globales por defecto asociadas al usuario (copia de las categorías semilla del sistema).
- Al registrarse, se crea una cuenta por defecto llamada "Efectivo" con tipo CASH y moneda ARS.

### RN-002: Baja de cuenta
- Un usuario no puede eliminarse si tiene transacciones, cuentas o tarjetas activas.
- La eliminación es lógica (deshabilitar la cuenta, no borrar datos).

---

## 2. Cuentas

### RN-003: Creación de cuenta
- El nombre de cuenta debe ser único por usuario.
- El balance inicial puede ser 0 o un valor positivo (no negativo).

### RN-004: Eliminación de cuenta
- No se puede eliminar una cuenta que tenga transacciones asociadas.
- No se puede eliminar la cuenta asociada a una tarjeta de crédito activa.
- Para eliminar, primero deben reasignarse o eliminarse las transacciones.

### RN-005: Transferencias entre cuentas
- Una transferencia genera dos transacciones: un EGRESO en la cuenta origen y un INGRESO en la cuenta destino.
- Ambas transacciones se vinculan mediante `parent_transaction_id`.
- El monto debe ser positivo y menor o igual al saldo de la cuenta origen (si es CHECKING).

---

## 3. Tarjetas de Crédito

### RN-006: Días de cierre y vencimiento
- `closing_day` y `due_day` deben estar entre 1 y 31.
- Si `closing_day > due_day`, se asume que el vencimiento es al mes siguiente.
- El sistema debe recordar al usuario 3 días antes del cierre y 3 días antes del vencimiento.

### RN-007: Consumos con tarjeta
- Un consumo con tarjeta de crédito se registra como transacción EXTERNAL (no afecta el saldo de cuentas hasta el pago).
- Al pagar la tarjeta desde una cuenta CHECKING, se registra una transferencia a la cuenta asociada a la tarjeta.

### RN-008: Cuotas
- Una compra en cuotas genera N transacciones (una por cuota), cada una con su fecha de vencimiento.
- El monto de cada cuota se calcula como `monto_total / total_cuotas`, redondeado a 2 decimales.
- La diferencia de redondeo se aplica a la primera cuota.
- Todas las cuotas se crean con `is_paid = false` y se marcan como pagadas cuando se paga el resumen.

---

## 4. Transacciones

### RN-009: Tipos de transacción
- `INCOME`: incrementa el saldo de la cuenta.
- `EXPENSE`: decrementa el saldo de la cuenta.
- `TRANSFER`: no afecta el saldo neto del usuario (es movimiento entre cuentas propias).

### RN-010: Fecha de transacción
- No se permiten transacciones con fecha futura (mayor a hoy + 1 día).
- No se permiten transacciones con fecha anterior a 5 años desde la fecha actual.

### RN-011: Montos
- El monto debe ser distinto de cero.
- Para `INCOME` el monto debe ser positivo.
- Para `EXPENSE` el monto se almacena como valor negativo.
- Para `TRANSFER` el monto debe ser positivo (el signo lo determina la cuenta origen/destino).

### RN-012: Descripción
- La descripción es obligatoria y no puede estar vacía.
- Longitud máxima: 500 caracteres.

### RN-013: Modificación de transacciones
- Solo se puede modificar la categoría, el merchant, la descripción y las notas.
- No se puede modificar el monto, la cuenta, la fecha ni el tipo de una transacción existente.
- Si se necesita corregir, debe eliminarse y crearse una nueva.

### RN-014: Eliminación de transacciones
- Al eliminar una transacción, se revierte su efecto en el saldo de la cuenta.
- Si es una transferencia, se deben eliminar ambas transacciones (origen y destino).
- Si es una transacción con cuotas, todas las cuotas asociadas se eliminan en cascada.

### RN-015: Duplicación
- Se considera duplicado cuando existe otra transacción del mismo usuario con exactamente el mismo `amount`, `description`, `date` y `account_id`.
- Los duplicados se rechazan con error 409.

---

## 5. Categorías

### RN-016: Categorías del sistema
- Las categorías con `is_system = true` no pueden eliminarse ni modificarse.
- Las categorías del sistema son: Sueldo, Freelance, Inversiones, Aguinaldo, Bonos, Otros-ingresos, Combustible, Supermercado, Streaming, Restaurantes, Alquiler, Servicios, Educación, Salud, Transporte, Indumentaria, Entretenimiento, Internet, Seguros, Patente, Otros-egresos.

### RN-017: Categorías personalizadas
- El usuario puede crear, modificar y eliminar sus propias categorías.
- No puede haber dos categorías con el mismo nombre para el mismo usuario.
- Si se elimina una categoría con transacciones asociadas, esas transacciones quedan sin categoría (`category_id = null`).

### RN-018: Tipo de categoría
- Una categoría de tipo INCOME solo puede asignarse a transacciones de tipo INCOME.
- Una categoría de tipo EXPENSE solo puede asignarse a transacciones de tipo EXPENSE.
- Una transacción de tipo TRANSFER no lleva categoría.

---

## 6. Comercios (Merchants)

### RN-019: Merchant único
- El `normalized_name` debe ser único en el sistema (global, no por usuario).
- Si dos usuarios importan "YPF SAN MARTIN", ambos apuntan al mismo merchant "YPF".

### RN-020: Asignación de categoría a merchant
- Un merchant puede tener una categoría por defecto (asignada por el sistema o por el usuario).
- Cuando se asigna una categoría a un merchant, todas las transacciones futuras de ese merchant se clasifican automáticamente.
- El usuario puede sobreescribir la categoría de una transacción individual sin afectar la regla del merchant.

---

## 7. Importación

### RN-021: Formato de archivo
- Solo se aceptan archivos CSV (`.csv`) y Excel (`.xlsx`).
- Tamaño máximo: 10 MB.
- El archivo debe tener codificación UTF-8.

### RN-022: Columnas requeridas (CSV bancario)
- `fecha`, `descripcion`, `importe`.
- Si falta alguna columna requerida, el import se rechaza con error.

### RN-023: Hash de archivo
- Se calcula SHA-256 del contenido del archivo.
- Si el mismo hash ya existe para el mismo usuario sin importar el estado, se rechaza con error 409 (archivo duplicado).
- Esto evita importaciones duplicadas accidentales.

### RN-024: Procesamiento por lotes
- Cada fila del archivo se procesa de forma independiente.
- Si una fila falla, las demás continúan procesándose.
- Al finalizar, `ImportJob.status` refleja el resultado general:
  - `COMPLETED`: todas las filas se importaron.
  - `PARTIAL`: algunas filas fallaron.
  - `FAILED`: todas las filas fallaron.

### RN-025: Validación de filas
- Cada fila debe tener una fecha válida, un monto numérico y una descripción no vacía.
- Las filas inválidas se registran en `TransactionRaw` con status `ERROR` y `error_message` descriptivo.
- Las filas duplicadas contra transacciones existentes se marcan como `DUPLICATE`.

### RN-026: Sobrescritura de clasificación
- Si el usuario ya clasificó manualmente un merchant (sobrescribió su categoría), esa preferencia se respeta en futuras importaciones.

---

## 8. Presupuestos

### RN-027: Período del presupuesto
- Un presupuesto puede ser semanal, mensual, anual o personalizado (fechas start/end).
- Al iniciar un nuevo período, el presupuesto se reinicia (si es `MONTHLY`, el primer día de cada mes).

### RN-028: Límite por categoría
- Solo puede haber un presupuesto activo por categoría y período.
- Si se crea un nuevo presupuesto para la misma categoría y período, el anterior se desactiva.

### RN-029: Alerta de presupuesto
- Cuando se alcanza el 80% del presupuesto, se genera una notificación.
- Cuando se alcanza el 100%, se genera una alerta crítica.

### RN-030: Consumo del presupuesto
- El consumo se calcula sumando todas las transacciones EXPENSE de la categoría en el período.
- No incluye transacciones de tipo TRANSFER.
- Incluye transacciones manuales e importadas.

---

## 9. Gastos Recurrentes

### RN-031: Generación automática
- El sistema puede detectar gastos recurrentes analizando transacciones repetitivas (mismo monto, misma descripción, mismo merchant, rango de días similar).
- La detección se ejecuta después de cada importación.

### RN-032: Próximo vencimiento
- `next_date` se calcula como la próxima fecha según `day_of_month` y `frequency`.
- Si `day_of_month` ya pasó en el mes actual, la próxima es el mes siguiente.

### RN-033: Recordatorio
- 5 días antes de `next_date`, el sistema debe notificar al usuario.

---

## 10. Dashboard y Reportes

### RN-034: Cálculo de saldo actual
- `saldo_actual = balance_inicial + SUM(INCOME) - SUM(EXPENSE)` para el período indicado.
- No se incluyen transacciones futuras.

### RN-035: Gastos por categoría
- Agrupa transacciones EXPENSE por categoría en el período.
- Muestra monto total y porcentaje sobre el total de gastos.

### RN-036: Evolución mensual
- Comparación mes a mes de ingresos y gastos.
- Muestra variación porcentual respecto al mes anterior.

### RN-037: Proyección financiera
- Basada en el promedio de gastos de los últimos 3 meses + gastos recurrentes.
- La proyección se calcula como: `saldo_actual + ingresos_pendientes - gastos_proyectados`.
- Incluye cuotas próximas a vencer.

### RN-038: Cuotas pendientes
- Lista todas las cuotas con `is_paid = false` y `due_date` en el futuro.
- Agrupadas por tarjeta de crédito.
- Muestra total adeudado y detalle por compra.

---

## 11. Insights Automáticos (Fase 1)

### RN-039: Variación respecto al mes anterior
- Si `gasto_mes_actual > gasto_mes_anterior * 1.10` → "Gastaste X% más que el mes pasado en [categoría]".
- Si `gasto_mes_actual < gasto_mes_anterior * 0.90` → "Gastaste X% menos que el mes pasado en [categoría]".

### RN-040: Variación vs promedio
- Si el gasto en una categoría supera en 20% el promedio de los últimos 3 meses → alerta.
- Si el gasto en una categoría está por debajo del 50% del promedio → felicitación/buen hábito.

### RN-041: Presupuesto excedido
- Si el consumo de una categoría superó el presupuesto → alerta.
- Si el consumo está por debajo del 50% → reconocimiento.

### RN-042: Proyección negativa
- Si la proyección de fin de mes es negativa → alerta crítica.
- Si la proyección es positiva pero menor al 10% de los ingresos → alerta preventiva.

### RN-043: Detección de picos
- Si un gasto individual supera el 30% del total de gastos del mes → se marca como gasto significativo.

---

## 12. Chat Financiero (Fase 2)

### RN-044: Alcance del chat
- El asistente solo puede responder preguntas basadas en datos del usuario.
- No puede dar consejos financieros no respaldados por datos.
- No puede hacer proyecciones no calculadas por el motor financiero.

### RN-045: Contexto
- Cada request al chat incluye un contexto estructurado generado por el motor financiero.
- El LLM no tiene acceso directo a la base de datos.
- El LLM no tiene acceso a datos de otros usuarios.

---

## 13. Notificaciones

### RN-046: Tipos de notificación
- `BUDGET_ALERT`: presupuesto cercano o excedido.
- `RECURRING_REMINDER`: gasto recurrente próximo.
- `CARD_CLOSING`: cierre de tarjeta próximo.
- `CARD_DUE`: vencimiento de tarjeta próximo.
- `INSIGHT`: insight automático generado.
- `IMPORT_COMPLETE`: importación finalizada.

### RN-047: Frecuencia
- Las notificaciones se generan una vez por evento.
- No se repiten a menos que el usuario no las haya visto.

---

## 14. Moneda

### RN-048: Moneda por defecto
- El usuario puede configurar su moneda por defecto (ARS o USD).
- Todas las transacciones nuevas usan la moneda por defecto del usuario.
- Al mostrar montos en USD, se puede mostrar el equivalente en ARS (conversión manual que el usuario debe configurar).

### RN-049: Conversión entre monedas
- No hay conversión automática en V1.
- Si un usuario tiene cuentas en ARS y USD, los totales del dashboard se muestran por separado.
- El usuario puede configurar manualmente un tipo de cambio para visualización.

---

## 15. Eliminación de datos

### RN-050: Eliminación lógica
- Las entidades principales (User, Account, CreditCard, Transaction, Category) se eliminan de forma lógica cuando corresponda.
- Los datos se conservan por 90 días después de la eliminación lógica antes del purge real.
- ImportJob, TransactionRaw y AuditLog son de solo lectura una vez creados.
