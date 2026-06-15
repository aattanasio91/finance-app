---
name: react
description: >
  Use when developing frontend applications with React, designing components,
  managing state, handling forms, or discussing React best practices. Covers
  hooks, component patterns, state management, performance, testing, and
  project structure.
---

# React

## Estructura de proyecto

```
src/
├── components/          # UI components
│   ├── ui/              # Atomic: Button, Input, Modal, Card
│   └── layout/          # Header, Sidebar, Footer
├── features/            # Feature-based modules
│   ├── auth/
│   │   ├── components/  # Auth-specific components
│   │   ├── hooks/       # Auth-specific hooks
│   │   └── pages/       # Login, Register pages
│   └── users/
├── hooks/               # Global custom hooks
├── lib/                 # Utilities, API client, helpers
├── types/               # TypeScript types/interfaces
├── pages/               # Route pages (Next.js: app/ o pages/)
├── stores/              # State management (Zustand, Redux)
├── services/            # API calls
├── styles/              # Global styles, theme
├── __tests__/           # Tests
├── App.tsx
└── main.tsx
```

## Hooks esenciales

### useState

```tsx
const [count, setCount] = useState(0);
const [user, setUser] = useState<User | null>(null);
```

- Nunca mutés el estado directamente → siempre usá el setter.
- Si el estado deriva de props → usá `useMemo` en vez de `useState`.

### useEffect

```tsx
useEffect(() => {
  fetchUsers().then(setUsers);
}, []); // Solo al montar

useEffect(() => {
  const sub = socket.on("message", handleMessage);
  return () => sub.unsubscribe(); // Cleanup
}, [userId]);
```

- Siempre limpiá side effects (subscriptions, timers, abort controllers).
- No abuses de `useEffect`: muchos casos se resuelven con event handlers, `useMemo`, o queries libraries.

### useMemo & useCallback

```tsx
const sortedItems = useMemo(() =>
  items.sort((a, b) => a.name.localeCompare(b.name)),
  [items]
);

const handleClick = useCallback((id: string) => {
  setSelected(id);
}, []);
```

- Usalos solo si hay un costo de cómputo o rerenders medibles.
- No es necesario wrappear todo. La optimización prematura no ayuda.

### useRef

```tsx
const inputRef = useRef<HTMLInputElement>(null);
const intervalRef = useRef<number>();

// Para valores mutables que no causan rerender
const renderCount = useRef(0);
```

### Custom hooks

Extraé lógica repetitiva a custom hooks:

```tsx
function useUsers() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    fetchUsers({ signal: controller.signal })
      .then(setUsers)
      .catch(setError)
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, []);

  return { users, loading, error };
}
```

## Component patterns

### Composition

```tsx
function Card({ title, children }: PropsWithChildren<{ title: string }>) {
  return (
    <div className="rounded-lg border p-4">
      <h2>{title}</h2>
      {children}
    </div>
  );
}

// Uso
<Card title="Users">
  <UserList users={users} />
</Card>
```

### Compound components

```tsx
function Select<T extends string>({ children, value, onChange }: SelectProps<T>) {
  return (
    <select value={value} onChange={e => onChange(e.target.value as T)}>
      {children}
    </select>
  );
}

Select.Option = function Option({ value, children }: { value: string; children: ReactNode }) {
  return <option value={value}>{children}</option>;
};

// Uso
<Select value={role} onChange={setRole}>
  <Select.Option value="admin">Admin</Select.Option>
  <Select.Option value="user">User</Select.Option>
</Select>
```

### Render props / Children as function (raro, pero útil)

```tsx
function DataFetcher<T>({ url, children }: { url: string; children: (data: T) => ReactNode }) {
  const { data } = useFetch<T>(url);
  return data ? children(data) : <Spinner />;
}
```

## State management

Elegí según la complejidad:

| Herramienta | Cuándo usarla |
|-------------|---------------|
| `useState` + lifting state up | Estado local o compartido entre pocos componentes cercanos |
| `useReducer` | Lógica de estado compleja (múltiples sub-valores, transiciones) |
| **Zustand** | Estado global mediano (preferido sobre Redux hoy) |
| **TanStack Query** | Estado del servidor (caching, refetch, mutations) |
| Context API | Temas, autenticación, i18n (NO para estado que cambia frecuentemente) |
| Redux Toolkit | Equipos grandes, estado global muy complejo |

### Zustand

```tsx
import { create } from "zustand";

interface AuthStore {
  user: User | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const useAuthStore = create<AuthStore>((set) => ({
  user: null,
  login: async (email, password) => {
    const user = await api.login(email, password);
    set({ user });
  },
  logout: () => set({ user: null }),
}));
```

### TanStack Query

```tsx
function UserList() {
  const { data, isLoading, error } = useQuery({
    queryKey: ["users"],
    queryFn: fetchUsers,
  });

  const mutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  if (isLoading) return <Spinner />;
  if (error) return <Error message={error.message} />;
  return data.map(user => <UserCard key={user.id} user={user} />);
}
```

## Forms

```tsx
function LoginForm() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginInput) => {
    await loginMutation.mutateAsync(data);
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)}>
      <Input label="Email" {...register("email")} error={errors.email?.message} />
      <Input label="Password" type="password" {...register("password")} />
      <Button type="submit" disabled={isSubmitting}>Login</Button>
    </form>
  );
}
```

## Performance

- **React.memo**: solo para componentes que renderizan frecuentemente con las mismas props.
- **Virtual list**: `@tanstack/react-virtual` para listas de +500 items.
- **Lazy loading**: `React.lazy(() => import("./HeavyComponent"))` con `<Suspense>`.
- **Code splitting**: dividí el bundle por rutas (React Router lazy).
- **Debounce**: búsqueda en inputs con `useDebounce` o librería.
- **Imágenes lazy**: `loading="lazy"` nativo en `<img>`.

## Testing

```tsx
import { render, screen, fireEvent } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { LoginForm } from "./LoginForm";

describe("LoginForm", () => {
  it("debería mostrar error con email inválido", async () => {
    render(<LoginForm />);

    await userEvent.type(screen.getByLabelText("Email"), "invalido");
    await userEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(screen.getByText("Email inválido")).toBeInTheDocument();
  });

  it("debería llamar onSubmit con datos válidos", async () => {
    const onSubmit = vi.fn();
    render(<LoginForm onSubmit={onSubmit} />);

    await userEvent.type(screen.getByLabelText("Email"), "test@mail.com");
    await userEvent.type(screen.getByLabelText("Password"), "password123");
    await userEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(onSubmit).toHaveBeenCalledWith({
      email: "test@mail.com",
      password: "password123",
    });
  });
});
```

## Buenas prácticas

- **TypeScript siempre**: tipá props, estado, eventos, respuestas de API.
- **Composición > herencia**: preferí composición de componentes.
- **Colocar estado lo más abajo posible**: lifting state up solo cuando es necesario.
- **Separación de concerns**: UI components (presentacionales) ≠ Container components (lógica).
- **Naming consistente**: hooks con `use*`, handlers con `handle*`, props booleanas con `is*`/`has*`.
- **Error boundaries**: al menos uno global para capturar errores inesperados.
- **Accesibilidad**: roles, aria labels, manejo de foco, contraste.
- **Mantené components pequeños**: si un componente pasa las 150 líneas, dividilo.
- **No dependas de índices como `key`** → usá IDs únicos estables.
