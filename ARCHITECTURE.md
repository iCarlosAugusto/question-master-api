# Architecture Documentation

## 🎯 System Overview
**Gamified Question API** é uma API REST para gerenciamento de questões gamificadas, inspirada no Duolingo e Trivia Track.

## 🏗️ Architecture Style
**Arquitetura em Camadas (Layered Architecture)**

```
┌─────────────────────────────────────────┐
│         Controllers Layer               │  ← HTTP/REST Endpoints
│   (AuthController, QuestionController)  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │  ← Business Logic
│   (UserService, QuestionService)        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Repository Layer                │  ← Data Access
│   (UserRepository, QuestionRepository)  │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Database (PostgreSQL)           │
└─────────────────────────────────────────┘
```

## 🔐 Security Architecture

### Authentication Flow
```
1. User sends credentials → AuthController
2. Validates with UserService
3. Generates JWT token with JwtTokenProvider
4. Returns token to client
5. Client includes token in Authorization header
6. JwtAuthenticationFilter validates token on each request
7. CustomUserDetailsService loads user details
8. Spring Security authorizes based on roles
```

### Security Components
- **JwtTokenProvider**: Cria e valida tokens JWT
- **JwtAuthenticationFilter**: Intercepta requisições e valida token
- **CustomUserDetailsService**: Carrega detalhes do usuário para autenticação
- **SecurityConfig**: Configura regras de autorização por endpoint

## 📊 Data Model

### Core Entities

#### User
```kotlin
- id: UUID (PK)
- role: AppRole (USER, ADMIN)
- displayName: String?
- createdAt: Instant
```

#### Subject (Matéria)
```kotlin
- id: Long (PK)
- name: String (unique)
- topics: List<Topic> (1:N)
```

#### Topic (Tópico)
```kotlin
- id: Long (PK)
- name: String
- subjectId: Long (FK → Subject)
```

#### Question
```kotlin
- id: UUID (PK)
- text: String
- year: Short?
- questionType: QuestionType (MULTIPLE_CHOICE, TRUE_FALSE)
- topicId: Long (FK → Topic)
- alternatives: List<Alternative> (1:N)
```

#### Alternative
```kotlin
- id: Long (PK)
- text: String
- isCorrect: Boolean
- questionId: UUID (FK → Question)
```

#### Answer (Resposta do usuário)
```kotlin
- id: UUID (PK)
- userId: UUID (FK → User)
- questionId: UUID (FK → Question)
- alternativeId: Long (FK → Alternative)
- isCorrect: Boolean
- answeredAt: Instant
```

## 🔄 Database Migration Strategy

**Flyway** gerencia todas as migrações:
- **V1**: Create users table
- **V2**: Create subjects table
- **V3**: Create topics table
- **V4**: Create questions table
- **V5**: Create alternatives table
- **V6**: Create answers table
- **V7**: Create indexes

⚠️ **IMPORTANTE**: 
- Hibernate está configurado como `validate` (não cria/altera schema)
- Apenas Flyway deve modificar o schema do banco

## 🌐 API Design

### Endpoint Patterns
```
/api/auth/*        - Autenticação (público)
/api/users/*       - Gerenciamento de usuários (autenticado)
/api/subjects/*    - CRUD de matérias (GET público, CUD admin)
/api/topics/*      - CRUD de tópicos (GET público, CUD admin)
/api/questions/*   - CRUD de questões (GET público, CUD admin)
/api/answers/*     - Respostas dos usuários (autenticado)
```

### Authorization Rules
- **Público**: GET em questions, subjects, topics
- **USER**: POST em answers, GET /users/me
- **ADMIN**: CUD em questions, subjects, topics

## 🔧 Configuration Management

### Profiles
- **dev**: Desenvolvimento local (application-dev.properties)
- **docker**: Container Docker (application-docker.properties)
- **prod**: Produção (application-prod.properties)
- **test**: Testes automatizados (application-test.properties)

### Key Configurations

#### Database Connection
```properties
# Supabase PostgreSQL com pooling
spring.datasource.url=jdbc:postgresql://aws-1-us-east-2.pooler.supabase.com:5432/postgres
# HikariCP pool configurado para evitar leaks
spring.datasource.hikari.maximum-pool-size=10
```

#### JPA/Hibernate
```properties
# Apenas validação - NÃO criar/alterar schema
spring.jpa.hibernate.ddl-auto=validate
```

#### Flyway
```properties
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
```

## 🧩 Design Patterns Used

### 1. **Repository Pattern**
Abstrai acesso a dados com Spring Data JPA
```kotlin
interface UserRepository : JpaRepository<User, UUID>
```

### 2. **DTO Pattern**
Separa entidades de domínio de objetos de transferência
```kotlin
data class CreateQuestionRequest(...)  // Input
data class QuestionResponse(...)       // Output
```

### 3. **Service Layer Pattern**
Encapsula lógica de negócio
```kotlin
@Service
class QuestionService(...)
```

### 4. **Builder Pattern**
Construção de objetos complexos (usado em entidades)

### 5. **Filter Chain Pattern**
JwtAuthenticationFilter na cadeia de filtros do Spring Security

## 🔍 Important Implementation Details

### JWT Token Management
- **Secret**: Configurado em properties (deve ser seguro em prod)
- **Expiration**: 24 horas (86400000ms)
- **Format**: Bearer {token}
- **Claims**: userId, roles

### Pagination
- Questões suportam paginação via `Pageable`
- Resposta no formato `PagedResponse<T>`

### Error Handling
- `GlobalExceptionHandler` para tratamento centralizado
- `ResourceNotFoundException` para recursos não encontrados
- `BusinessException` para regras de negócio

## 🚀 Deployment Considerations

### Database
- **Supabase PostgreSQL** em produção
- Connection pooling configurado
- Flyway gerencia schema

### Security
- JWT tokens (stateless)
- CORS configurado por profile
- Passwords devem usar BCrypt

### Monitoring
- Logs em DEBUG (dev) e INFO (prod)
- SQL logging habilitado em dev

## 🧪 Testing Strategy
- Testes unitários para services
- Testes de integração com banco H2 em memória
- Spring Security Test para endpoints protegidos

