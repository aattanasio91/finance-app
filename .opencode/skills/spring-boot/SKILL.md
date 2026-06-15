---
name: spring-boot
description: >
  Use when developing backend services with Spring Boot, designing REST APIs,
  configuring security, persistence, or discussing Spring Boot best practices.
  Covers project structure, annotations, dependency injection, data access,
  security, testing, and common patterns.
---

# Spring Boot

## Estructura de proyecto

```
src/main/java/com/company/project/
├── config/           # @Configuration, @Bean, settings
├── controller/       # @RestController, request handling
├── service/          # Business logic, @Service
├── repository/       # Data access, @Repository
├── domain/           # Entities, value objects
├── dto/              # Request/Response DTOs
├── exception/        # Custom exceptions, @ControllerAdvice
├── mapper/           # Entity ↔ DTO mapping
├── security/         # JWT, OAuth, filters
└── ProjectApplication.java

src/main/resources/
├── application.yml
├── application-dev.yml
└── application-prod.yml

src/test/java/com/company/project/
```

## Anotaciones clave

| Anotación | Propósito |
|-----------|-----------|
| `@SpringBootApplication` | Entry point, combo de @Configuration + @EnableAutoConfiguration + @ComponentScan |
| `@RestController` | Controller REST (@Controller + @ResponseBody) |
| `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` | Mapeo de endpoints HTTP |
| `@Service` | Capa de negocio |
| `@Repository` | Acceso a datos (traduce excepciones JPA a DataAccessException) |
| `@Entity`, `@Table`, `@Column`, `@Id`, `@GeneratedValue` | JPA mapping |
| `@Autowired` / `@RequiredArgsConstructor` | Inyección de dependencias |
| `@Valid`, `@Validated` | Validación de requests con Jakarta Validation |
| `@ControllerAdvice` | Manejo global de excepciones |
| `@ConfigurationProperties` | Binding de properties a POJO tipado |
| `@Transactional` | Manejo de transacciones |
| `@Cacheable`, `@CacheEvict` | Caching |

## REST API design

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userService.findAll(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Service layer

- Lógica de negocio en `@Service`, nunca en controllers ni repositories.
- Usá `@Transactional(readOnly = true)` en queries de solo lectura.
- Las transacciones deben estar en la capa de service, no repository.
- Lanzá excepciones de negocio (`DomainException`) en vez de devolver null.

```java
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

## Repository / JPA

- Preferí `Spring Data JPA` con `JpaRepository<T, ID>`.
- Usá `@Query` con JPQL para queries complejas. Native queries como último recurso.
- `Specification` o `QueryDSL` para filtros dinámicos.
- `@EntityGraph` para eager loading controlado (evitá N+1).
- `@MappedSuperclass` para campos comunes (createdAt, updatedAt).

```java
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);

    Page<User> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
```

## DTOs y validación

```java
public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 100) String password,
    @NotBlank @Size(max = 100) String name,
    @NotNull Role role
) {}

public record UserResponse(
    Long id,
    String email,
    String name,
    Role role,
    Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), user.getEmail(),
            user.getName(), user.getRole(),
            user.getCreatedAt()
        );
    }
}
```

## Exception handling

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        return ResponseEntity.status(ex.getHttpStatus())
                .body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Validation failed", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error"));
    }
}
```

## Security (Spring Security)

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Testing

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUser_deberiaRetornar201() throws Exception {
        var request = new CreateUserRequest("test@mail.com", "password123", "Test", Role.USER);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@mail.com"));
    }
}
```

```java
@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_deberiaRetornarUsuario() {
        var user = new User("test@mail.com", "encoded", "Test", Role.USER);
        userRepository.save(user);

        var result = userRepository.findByEmail("test@mail.com");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test@mail.com");
    }
}
```

## Configuración (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        format_sql: true

server:
  port: 8080

logging:
  level:
    com.company.project: DEBUG
```

## Buenas prácticas

- **No pongas lógica de negocio en controllers** → delegá a services.
- **Usá `@Valid` en los DTOs de request** → nunca confíes en input del cliente.
- **Evitá `@Autowired` en campos** → preferí constructor injection (o Lombok `@RequiredArgsConstructor`).
- **Usá `records` para DTOs** (Java 16+).
- **Configurá `ddl-auto: validate`** en prod. Usá Flyway o Liquibase para migrations.
- **Perfil de aplicación**: separá config por entorno (dev, staging, prod).
- **Mantené controllers delgados** (< 50 líneas idealmente).
- **Usá `@ExceptionHandler` global** → no try/catch en controllers.
- **Loggeá con SLF4J + Lombok `@Slf4j`**.
