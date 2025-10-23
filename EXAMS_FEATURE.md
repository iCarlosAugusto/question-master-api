# 📚 Feature: Sistema de Provas (Exams)

## 🎯 Visão Geral

Implementação completa de um sistema de provas para categorizar questões por tipo de exame (ENEM, Concurso, Vestibular, etc.).

---

## 🗂️ Estrutura Criada

### 1. **Enum ExamType**
```kotlin
enum class ExamType {
    CONCURSO,
    ENEM,
    VESTIBULAR,
    CERTIFICACAO,
    SIMULADO,
    OUTROS
}
```

### 2. **Entidade Exam**
- **Campos**:
  - `id`: Long (auto-increment)
  - `name`: String (obrigatório) - Nome da prova
  - `examType`: ExamType (obrigatório) - Tipo da prova
  - `institution`: String (opcional) - Instituição (ex: INEP, FUVEST, TRT)
  - `year`: Short (opcional) - Ano da prova
  - `description`: Text (opcional) - Descrição adicional
  - `isActive`: Boolean - Status ativo/inativo
  - `createdAt`: Timestamp
  - `updatedAt`: Timestamp
  - `questions`: Relacionamento 1:N com Question

### 3. **Migrations**

#### V8__create_exams_table.sql
- Cria tabela `exams`
- Adiciona índices para otimização
- Insere dados de exemplo (ENEM 2023/2022, Concursos, etc.)

#### V9__add_exam_to_questions.sql
- Adiciona coluna `exam_id` na tabela `questions`
- Cria foreign key constraint
- Adiciona índice para performance

---

## 📡 API Endpoints

### **Exams Controller** (`/api/exams`)

#### 1. **GET /api/exams** - Listar provas com filtros
```bash
GET /api/exams?page=0&size=20&examType=ENEM&year=2023&institution=INEP
```

**Query Parameters**:
- `page` (default: 0)
- `size` (default: 20)
- `examType` (opcional): CONCURSO | ENEM | VESTIBULAR | CERTIFICACAO | SIMULADO | OUTROS
- `year` (opcional): Ano da prova
- `institution` (opcional): Busca por nome da instituição (case-insensitive)

**Response**:
```json
{
  "content": [
    {
      "id": 1,
      "name": "ENEM 2023",
      "examType": "ENEM",
      "institution": "INEP",
      "year": 2023,
      "description": "Exame Nacional do Ensino Médio 2023",
      "isActive": true,
      "createdAt": "2025-01-01T00:00:00",
      "updatedAt": "2025-01-01T00:00:00",
      "questionCount": 45
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 5,
  "totalPages": 1,
  "last": true
}
```

#### 2. **GET /api/exams/summary** - Lista resumida de todas as provas
```bash
GET /api/exams/summary
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "ENEM 2023",
    "examType": "ENEM",
    "institution": "INEP",
    "year": 2023
  }
]
```

#### 3. **GET /api/exams/{id}** - Buscar prova por ID
```bash
GET /api/exams/1
```

#### 4. **POST /api/exams** - Criar prova (Admin apenas)
```bash
POST /api/exams
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Concurso TRF 2025",
  "examType": "CONCURSO",
  "institution": "TRF",
  "year": 2025,
  "description": "Tribunal Regional Federal 2025"
}
```

#### 5. **PUT /api/exams/{id}** - Atualizar prova (Admin apenas)
```bash
PUT /api/exams/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "ENEM 2023 - Atualizado",
  "isActive": true
}
```

#### 6. **DELETE /api/exams/{id}** - Deletar prova (Admin apenas)
```bash
DELETE /api/exams/1
Authorization: Bearer <token>
```

---

## 🔍 Mudanças nos Endpoints de Questions

### **⚠️ BREAKING CHANGE: Header X-Exam-Id é OBRIGATÓRIO**

#### **GET /api/questions** - Agora requer header X-Exam-Id

```bash
GET /api/questions?page=0&size=20&subjectIds=1,2&topicIds=3,4
X-Exam-Id: 1
Authorization: Bearer <token> (opcional)
```

**Headers Obrigatórios**:
- `X-Exam-Id`: ID da prova (Long)

**Query Parameters** (todos opcionais):
- `page` (default: 0)
- `size` (default: 20)
- `subjectIds`: Lista de IDs de matérias
- `topicIds`: Lista de IDs de tópicos
- `years`: Lista de anos
- `questionType`: MULTIPLE_CHOICE | TRUE_FALSE | etc.
- `answerStatus`: ANSWERED | UNANSWERED | CORRECT | INCORRECT

**Response**:
```json
{
  "items": [
    {
      "id": "uuid",
      "statement": "Qual é a capital do Brasil?",
      "subject": { "id": 1, "name": "Geografia" },
      "topics": [...],
      "exam": {
        "id": 1,
        "name": "ENEM 2023",
        "examType": "ENEM",
        "institution": "INEP",
        "year": 2023
      },
      "year": 2023,
      "alternatives": [...],
      "userAnswer": null
    }
  ],
  "page": 0,
  "pageSize": 20,
  "totalPages": 10,
  "totalItems": 200
}
```

---

## 📝 Criar/Atualizar Question com Exam

### **POST /api/questions**
```bash
POST /api/questions
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "statement": "Qual é a capital do Brasil?",
  "subjectId": 1,
  "examId": 1,  // <-- NOVO CAMPO (opcional)
  "year": 2023,
  "questionType": "MULTIPLE_CHOICE",
  "topicIds": [1, 2],
  "alternatives": [
    { "body": "Brasília", "isCorrect": true },
    { "body": "São Paulo", "isCorrect": false }
  ]
}
```

### **PUT /api/questions/{id}**
```bash
PUT /api/questions/{id}
Authorization: Bearer <admin-token>
Content-Type: application/json

{
  "statement": "Qual é a capital do Brasil?",
  "subjectId": 1,
  "examId": 2,  // <-- NOVO CAMPO (opcional)
  "year": 2023,
  "questionType": "MULTIPLE_CHOICE",
  "isActive": true,
  "topicIds": [1, 2],
  "alternatives": [...]
}
```

---

## 🔒 CORS Configuration

O header `X-Exam-Id` foi adicionado aos headers permitidos no CORS:

```kotlin
configuration.allowedHeaders = listOf(
    "Authorization",
    "Content-Type",
    "Accept",
    "Origin",
    "X-Requested-With",
    "X-Exam-Id"  // <-- NOVO
)
```

---

## 🎨 Frontend Integration Examples

### **React/TypeScript**

```typescript
// 1. Buscar lista de provas para dropdown
const exams = await fetch('http://localhost:8080/api/exams/summary')
  .then(res => res.json());

// 2. Buscar questões de uma prova específica
const questions = await fetch('http://localhost:8080/api/questions?page=0&size=20', {
  headers: {
    'X-Exam-Id': '1',  // <-- OBRIGATÓRIO
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
}).then(res => res.json());
```

### **Axios Configuration**

```typescript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para adicionar X-Exam-Id automaticamente
api.interceptors.request.use(config => {
  const selectedExamId = localStorage.getItem('selectedExamId');
  if (selectedExamId) {
    config.headers['X-Exam-Id'] = selectedExamId;
  }
  return config;
});

// Uso
const questions = await api.get('/questions', {
  params: { page: 0, size: 20 }
});
```

---

## 🗄️ Banco de Dados

### **Exemplos de Queries**

#### Buscar todas as questões de uma prova
```sql
SELECT q.* 
FROM questions q 
WHERE q.exam_id = 1 
AND q.is_active = true;
```

#### Estatísticas por prova
```sql
SELECT 
  e.name,
  e.exam_type,
  COUNT(q.id) as total_questions
FROM exams e
LEFT JOIN questions q ON q.exam_id = e.id
GROUP BY e.id, e.name, e.exam_type
ORDER BY e.year DESC;
```

---

## ✅ Checklist de Implementação

- [x] Criar enum `ExamType`
- [x] Criar entidade `Exam`
- [x] Criar migration para tabela `exams`
- [x] Atualizar entidade `Question` com relacionamento
- [x] Criar migration para adicionar `exam_id` em `questions`
- [x] Criar `ExamRepository` com queries customizadas
- [x] Criar DTOs (Request e Response)
- [x] Criar `ExamService` com CRUD completo
- [x] Criar `ExamController` com todos os endpoints
- [x] Atualizar `QuestionService` para incluir `Exam`
- [x] Atualizar `QuestionController` com header `X-Exam-Id`
- [x] Atualizar `QuestionRepository` para filtrar por `examId`
- [x] Atualizar CORS para permitir header `X-Exam-Id`
- [x] Atualizar Swagger documentation

---

## 🚀 Como Testar

### 1. Executar as migrations
```bash
./gradlew flywayMigrate
```

### 2. Iniciar a aplicação
```bash
./gradlew bootRun
```

### 3. Testar endpoints

```bash
# Listar provas
curl http://localhost:8080/api/exams/summary

# Buscar questões (com header obrigatório)
curl -H "X-Exam-Id: 1" http://localhost:8080/api/questions
```

---

## 📊 Dados de Exemplo Inseridos

As seguintes provas foram inseridas automaticamente:

1. **ENEM 2023** - INEP
2. **ENEM 2022** - INEP
3. **Concurso Público TRT 2023** - TRT
4. **Vestibular FUVEST 2023** - FUVEST
5. **OAB 1ª Fase 2023** - OAB

---

## 🔄 Migrações Futuras (Sugestões)

- Adicionar campo `difficulty` em questions (EASY, MEDIUM, HARD)
- Criar tabela de performance por prova por usuário
- Implementar sistema de simulados baseados em provas reais
- Adicionar tags/categorias adicionais para questões
- Implementar sistema de favoritos de questões

---

## 📝 Notas Importantes

1. **Breaking Change**: Todas as chamadas ao endpoint `/api/questions` agora **requerem** o header `X-Exam-Id`
2. O campo `examId` em `CreateQuestionRequest` e `UpdateQuestionRequest` é **opcional**
3. Questions antigas sem `exam_id` continuarão funcionando (campo nullable)
4. O frontend deve implementar um seletor de prova antes de carregar questões
5. Admins podem criar novas provas via API

---

## 🆘 Troubleshooting

### Erro: "Required request header 'X-Exam-Id' for method parameter type Long is not present"

**Solução**: Adicione o header `X-Exam-Id` em todas as requisições para `/api/questions`:
```bash
curl -H "X-Exam-Id: 1" http://localhost:8080/api/questions
```

### CORS Error no frontend

**Solução**: Verifique se `X-Exam-Id` está na lista de `allowedHeaders` no `SecurityConfig.kt`

---

Implementação completa! 🎉

