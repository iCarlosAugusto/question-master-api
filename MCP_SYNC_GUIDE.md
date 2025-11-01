# 🔄 Guia de Sincronização MCP Server ↔ API

## 📋 Como o MCP Server Descobre Mudanças na API?

**O MCP Server NÃO descobre mudanças automaticamente!** Ele precisa ser atualizado manualmente quando a API muda.

### Por que isso acontece?

O MCP Server faz chamadas HTTP diretas para os endpoints da API. Se você:
- ✅ Muda um endpoint
- ✅ Adiciona novos parâmetros obrigatórios
- ✅ Remove endpoints
- ✅ Muda a estrutura de resposta

**O MCP Server precisa ser atualizado manualmente!**

---

## 🔍 Formas de Detectar Mudanças na API

### 1. **OpenAPI / Swagger** (Recomendado)
A API expõe documentação OpenAPI em:
- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

**Vantagens:**
- Especificação padronizada
- Pode ser usado para gerar clientes automaticamente
- Documentação sempre atualizada

**Como usar:**
```bash
# Baixar a especificação OpenAPI
curl http://localhost:8080/v3/api-docs > openapi.json

# Comparar com versão anterior
diff openapi-old.json openapi.json
```

### 2. **Versionamento da API**
Adicionar versionamento explícito:
- `/api/v1/subjects`
- `/api/v2/subjects`

**Vantagens:**
- Permite manter compatibilidade
- Facilita migração gradual
- Clareza sobre mudanças

### 3. **Documentação de CHANGELOG**
Manter um arquivo `CHANGELOG.md` documentando todas as mudanças:

```markdown
## [2.0.0] - 2024-01-15

### Changed
- **BREAKING:** `GET /api/subjects` agora requer `examSlug` como query parameter
- `POST /api/subjects` aceita `examId` opcional

### Added
- Relação OneToMany entre Exam e Subject
- Migration V11 para criar coluna `exam_id` em `subjects`
```

### 4. **Testes de Integração**
Testes que falham quando a API muda:
- Detecção automática de breaking changes
- Feedback imediato durante desenvolvimento

---

## 🛠️ Melhor Forma de Atualizar o MCP Server

### Abordagem Recomendada: **Processo Manual Controlado**

#### 1. **Antes de Mudar a API:**
- [ ] Documentar mudanças no CHANGELOG
- [ ] Atualizar OpenAPI/Swagger
- [ ] Criar issue/tarefa para atualizar MCP Server

#### 2. **Depois de Mudar a API:**
- [ ] Atualizar `api-client.ts` no MCP Server
- [ ] Atualizar tipos em `types.ts`
- [ ] Atualizar handlers em `index.ts`
- [ ] Testar todas as funções afetadas
- [ ] Atualizar documentação do MCP Server

#### 3. **Checklist de Atualização:**

```markdown
### Checklist de Mudanças
- [ ] `api-client.ts` - Endpoints atualizados
- [ ] `types.ts` - Tipos atualizados
- [ ] `index.ts` - Handlers atualizados
- [ ] Tool schemas atualizados
- [ ] Documentação atualizada
- [ ] Testes passando
```

---

## 📝 Exemplo: Mudanças Necessárias para Subjects

### Mudanças na API:
1. `GET /api/subjects` → Agora requer `?examSlug={slug}`
2. `POST /api/subjects` → Aceita `examId` opcional no body
3. `PUT /api/subjects/{id}` → Aceita `examId` opcional no body

### Atualizações Necessárias no MCP Server:

#### 1. `api-client.ts`
```typescript
// ANTES
export async function listSubjects(config: ApiConfig) {
  return makeRequest(config, "/api/subjects");
}

// DEPOIS
export async function listSubjects(config: ApiConfig, examSlug: string) {
  return makeRequest(config, `/api/subjects?examSlug=${examSlug}`);
}
```

#### 2. `types.ts`
```typescript
// ANTES
export interface SubjectPayload {
  name: string;
  description?: string;
}

// DEPOIS
export interface SubjectPayload {
  name: string;
  description?: string;
  examId?: number; // NOVO
}
```

#### 3. `index.ts` - Tool Schema
```typescript
// ANTES
{
  name: "list_subjects",
  inputSchema: {
    type: "object",
    properties: {},
  },
}

// DEPOIS
{
  name: "list_subjects",
  inputSchema: {
    type: "object",
    properties: {
      examSlug: {
        type: "string",
        description: "Slug of the exam (REQUIRED)",
      },
    },
    required: ["examSlug"],
  },
}
```

---

## 🚀 Automação Futura (Opcional)

### Opção 1: Script de Sincronização
Criar script que:
1. Baixa OpenAPI spec
2. Compara com versão anterior
3. Detecta breaking changes
4. Sugere atualizações no código

### Opção 2: Geração Automática de Cliente
Usar ferramentas como `openapi-generator`:
```bash
openapi-generator generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-fetch \
  -o src/generated-client
```

### Opção 3: CI/CD Integration
No pipeline:
- [ ] Testa MCP Server contra API
- [ ] Falha se há incompatibilidade
- [ ] Gera report de mudanças

---

## 📚 Próximos Passos

1. ✅ **Atualizar MCP Server** com as mudanças de Subjects
2. 📝 **Criar CHANGELOG.md** na API
3. 📊 **Atualizar OpenAPI** (já está atualizado)
4. 🔄 **Documentar processo** de sincronização

---

## ⚠️ IMPORTANTE

**Sempre atualize o MCP Server ANTES de fazer deploy da API em produção!**

Ordem recomendada:
1. Desenvolver mudança na API
2. Atualizar MCP Server
3. Testar localmente
4. Fazer deploy
