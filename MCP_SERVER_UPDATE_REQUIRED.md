# 🔄 Atualizações Necessárias no MCP Server

## ⚠️ BREAKING CHANGES na API

A API foi atualizada e o MCP Server precisa ser sincronizado. Veja as mudanças abaixo:

---

## 📋 Mudanças em Subjects

### 1. **`listSubjects` - AGORA REQUER `examSlug`**

#### ❌ ANTES:
```typescript
// api-client.ts
export async function listSubjects(config: ApiConfig) {
  return makeRequest(config, "/api/subjects");
}

// index.ts - Tool Schema
{
  name: "list_subjects",
  inputSchema: {
    type: "object",
    properties: {},
  },
}

// index.ts - Handler
case "list_subjects": {
  const result = await apiClient.listSubjects(getConfig());
  // ...
}
```

#### ✅ DEPOIS (Obrigatório):
```typescript
// api-client.ts
export async function listSubjects(config: ApiConfig, examSlug: string) {
  return makeRequest(config, `/api/subjects?examSlug=${examSlug}`);
}

// index.ts - Tool Schema
{
  name: "list_subjects",
  description: "List all subjects for a specific exam. Requires examSlug parameter.",
  inputSchema: {
    type: "object",
    properties: {
      examSlug: {
        type: "string",
        description: "Slug of the exam (REQUIRED) - URL-friendly identifier like 'enem-2024'",
      },
    },
    required: ["examSlug"],
  },
}

// index.ts - Handler
case "list_subjects": {
  const { examSlug } = args as { examSlug: string };
  const result = await apiClient.listSubjects(getConfig(), examSlug);
  // ...
}
```

---

### 2. **`createSubject` - ACEITA `examId` OPCIONAL**

#### ❌ ANTES:
```typescript
// types.ts
export interface SubjectPayload {
  name: string;
  description?: string;
}

// index.ts - Tool Schema
{
  name: "create_subject",
  inputSchema: {
    type: "object",
    properties: {
      name: { type: "string" },
      description: { type: "string" },
    },
    required: ["name"],
  },
}
```

#### ✅ DEPOIS (Obrigatório):
```typescript
// types.ts
export interface SubjectPayload {
  name: string;
  description?: string;
  examId?: number; // NOVO - Opcional
}

// index.ts - Tool Schema
{
  name: "create_subject",
  description: "Create a new subject/category. Requires ADMIN authentication. Can optionally link to an exam.",
  inputSchema: {
    type: "object",
    properties: {
      name: {
        type: "string",
        description: "Name of the subject (e.g., 'Mathematics', 'History')",
      },
      description: {
        type: "string",
        description: "Optional description of the subject",
      },
      examId: {
        type: "number",
        description: "Optional: ID of the exam to link this subject to",
      },
    },
    required: ["name"],
  },
}
```

---

### 3. **`updateSubject` - ACEITA `examId` OPCIONAL**

#### ❌ ANTES:
```typescript
// index.ts - Tool Schema
{
  name: "update_subject",
  inputSchema: {
    type: "object",
    properties: {
      id: { type: "number" },
      name: { type: "string" },
      description: { type: "string" },
    },
    required: ["id"],
  },
}
```

#### ✅ DEPOIS (Obrigatório):
```typescript
// index.ts - Tool Schema
{
  name: "update_subject",
  description: "Update an existing subject. Requires ADMIN authentication. Can optionally link/unlink from an exam.",
  inputSchema: {
    type: "object",
    properties: {
      id: {
        type: "number",
        description: "ID of the subject to update",
      },
      name: {
        type: "string",
        description: "Updated name",
      },
      description: {
        type: "string",
        description: "Updated description",
      },
      examId: {
        type: "number",
        description: "Optional: ID of the exam to link this subject to. Pass null to unlink.",
      },
    },
    required: ["id"],
  },
}
```

---

## 📝 Checklist de Atualização

### Arquivo: `src/types.ts`
- [ ] Adicionar `examId?: number` em `SubjectPayload`

### Arquivo: `src/api-client.ts`
- [ ] Atualizar `listSubjects` para aceitar `examSlug: string`
- [ ] Mudar endpoint de `/api/subjects` para `/api/subjects?examSlug={examSlug}`

### Arquivo: `src/index.ts`
- [ ] Atualizar tool schema `list_subjects` para exigir `examSlug`
- [ ] Atualizar tool schema `create_subject` para aceitar `examId` opcional
- [ ] Atualizar tool schema `update_subject` para aceitar `examId` opcional
- [ ] Atualizar handler `list_subjects` para passar `examSlug`
- [ ] Atualizar descrições dos tools

---

## 🔍 Como Validar as Mudanças

### 1. Testar Localmente
```bash
# No MCP Server
cd /Users/carlos/Desktop/D3V/question-master-mcp-server

# Recompilar
npm run build

# Testar função
# Usar o MCP Inspector ou Cursor para testar list_subjects
```

### 2. Verificar Erros
- [ ] `list_subjects` sem `examSlug` deve retornar erro
- [ ] `list_subjects` com `examSlug` válido deve retornar subjects
- [ ] `create_subject` com `examId` deve vincular ao exam
- [ ] `update_subject` com `examId` deve atualizar o vínculo

---

## 📚 Referências

- **API Endpoint:** `GET /api/subjects?examSlug={slug}`
- **API Endpoint:** `POST /api/subjects` (body pode incluir `examId`)
- **API Endpoint:** `PUT /api/subjects/{id}` (body pode incluir `examId`)
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

---

## ⚠️ IMPORTANTE

**ATENÇÃO:** Se você fizer deploy da API atualizada ANTES de atualizar o MCP Server, o MCP Server vai quebrar!

**Ordem recomendada:**
1. ✅ Atualizar MCP Server
2. ✅ Testar localmente
3. ✅ Fazer deploy da API
4. ✅ Verificar funcionamento

---

## 🚀 Próximos Passos

Após atualizar o MCP Server:
1. Testar todas as funções relacionadas a subjects
2. Atualizar documentação do MCP Server
3. Atualizar versão do MCP Server (ex: 2.2.0)
4. Commit e push das mudanças
