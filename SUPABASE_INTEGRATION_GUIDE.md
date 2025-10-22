# 🔐 Guia de Integração Supabase Auth

Este guia explica como integrar completamente o Supabase Auth com a Question Master API.

## 📋 O que foi implementado

### ✅ 1. Migration V9 - Integração com Supabase
Arquivo: `V9__setup_supabase_auth_integration.sql`

**Recursos implementados:**
- ✅ Tabela `profiles` com campos `email` e `updated_at`
- ✅ Trigger `on_auth_user_created` - Cria profile automaticamente
- ✅ Trigger `on_auth_user_updated` - Sincroniza atualizações
- ✅ Trigger `on_auth_user_deleted` - Remove profile quando usuário é deletado
- ✅ Row Level Security (RLS) policies
- ✅ Índices otimizados

### ✅ 2. Entidade User Atualizada
- ✅ Campo `email: String?`
- ✅ Campo `updatedAt: LocalDateTime`
- ✅ Mapeamento correto para tabela `profiles`

### ✅ 3. UserRepository Atualizado
Novos métodos:
- `findByEmail(email: String): User?`
- `existsByEmail(email: String): Boolean`

### ✅ 4. UserService Atualizado
Novos métodos:
- `getOrCreateUser()` - Busca ou cria usuário
- `getUserByEmail()` - Busca por email
- `existsByEmail()` - Verifica existência por email

## 🚀 Como Usar

### **Passo 1: Executar a Migration**

A migration V9 será executada automaticamente pelo Flyway quando você iniciar a aplicação.

```bash
./gradlew bootRun
```

### **Passo 2: Configurar o Supabase**

#### 2.1. Obter as credenciais do Supabase

No Supabase Dashboard:
1. Acesse **Project Settings** → **API**
2. Anote:
   - `Project URL`: `https://[seu-projeto].supabase.co`
   - `anon/public key`: Para frontend
   - `service_role key`: Para backend (NUNCA exponha!)

#### 2.2. Testar a criação de usuário

No Supabase Dashboard → **SQL Editor**, execute:

```sql
-- Criar um usuário de teste
INSERT INTO auth.users (
    id,
    email,
    encrypted_password,
    email_confirmed_at,
    raw_user_meta_data,
    aud,
    role,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'test@example.com',
    crypt('password123', gen_salt('bf')),
    now(),
    '{"role": "USER", "display_name": "Test User"}'::jsonb,
    'authenticated',
    'authenticated',
    now(),
    now()
);

-- Verificar se o profile foi criado automaticamente
SELECT * FROM public.profiles WHERE email = 'test@example.com';
```

### **Passo 3: Integrar no Frontend**

#### Exemplo com JavaScript/TypeScript:

```typescript
import { createClient } from '@supabase/supabase-js'

const supabaseUrl = 'https://[seu-projeto].supabase.co'
const supabaseKey = '[sua-anon-key]'
const supabase = createClient(supabaseUrl, supabaseKey)

// Registro de usuário
async function signUp(email: string, password: string, displayName: string) {
  const { data, error } = await supabase.auth.signUp({
    email,
    password,
    options: {
      data: {
        display_name: displayName,
        role: 'USER'
      }
    }
  })
  
  if (error) throw error
  
  // O trigger criará automaticamente o profile!
  console.log('User created:', data.user?.id)
  return data
}

// Login
async function signIn(email: string, password: string) {
  const { data, error } = await supabase.auth.signInWithPassword({
    email,
    password
  })
  
  if (error) throw error
  
  // Token JWT para usar no backend
  const token = data.session?.access_token
  console.log('JWT Token:', token)
  
  return { user: data.user, token }
}

// Chamar API do backend com o token
async function callBackend(token: string) {
  const response = await fetch('http://localhost:8080/api/questions', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  })
  
  return response.json()
}
```

### **Passo 4: Validar JWT do Supabase no Backend (Opcional)**

Se você quiser validar diretamente o JWT do Supabase:

```kotlin
// JwtTokenProvider.kt - Adicionar método
fun validateSupabaseToken(token: String): UUID? {
    try {
        val claims = Jwts.parserBuilder()
            .setSigningKey(supabaseJwtSecret) // JWT Secret do Supabase
            .build()
            .parseClaimsJws(token)
            .body
        
        val userId = claims.subject // sub = user.id
        return UUID.fromString(userId)
    } catch (e: Exception) {
        return null
    }
}
```

## 📊 Estrutura de Dados

### auth.users (Supabase - Gerenciado automaticamente)
```
id              UUID PRIMARY KEY
email           VARCHAR(255)
encrypted_password TEXT
email_confirmed_at TIMESTAMPTZ
raw_user_meta_data JSONB
role            TEXT
created_at      TIMESTAMPTZ
updated_at      TIMESTAMPTZ
```

### public.profiles (Sua aplicação)
```
id              UUID PRIMARY KEY (FK → auth.users.id)
role            VARCHAR(20) (USER, ADMIN)
display_name    TEXT
email           VARCHAR(255)
created_at      TIMESTAMPTZ
updated_at      TIMESTAMPTZ
```

## 🔄 Fluxo de Dados

```
1. Frontend → Supabase Auth
   └─ signUp() ou signIn()

2. Supabase Auth → auth.users
   └─ Cria/autentica usuário

3. TRIGGER → public.profiles
   └─ Cria profile automaticamente

4. Frontend → Obtém JWT token

5. Frontend → Backend API (com JWT no header)
   └─ Authorization: Bearer {token}

6. Backend → Valida token
   └─ Extrai user.id

7. Backend → Busca profile
   └─ SELECT * FROM profiles WHERE id = ?
```

## 🔒 Row Level Security (RLS)

As seguintes políticas foram implementadas:

### Para Usuários:
- ✅ Usuários podem **ver** apenas seu próprio profile
- ✅ Usuários podem **atualizar** apenas seu próprio profile

### Para Admins:
- ✅ Admins podem **ver** todos os profiles
- ✅ Admins podem **atualizar** qualquer profile
- ✅ Admins podem **deletar** profiles

### Para Serviço:
- ✅ Triggers podem **inserir** profiles automaticamente

## 🧪 Testes

### Teste 1: Criar usuário via SQL
```sql
-- No Supabase SQL Editor
INSERT INTO auth.users (
    id, email, encrypted_password, email_confirmed_at,
    raw_user_meta_data, aud, role, created_at, updated_at
)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001'::uuid,
    'admin@test.com',
    crypt('admin123', gen_salt('bf')),
    now(),
    '{"role": "ADMIN", "display_name": "Admin User"}'::jsonb,
    'authenticated',
    'authenticated',
    now(),
    now()
);

-- Verificar
SELECT * FROM public.profiles WHERE email = 'admin@test.com';
```

### Teste 2: Atualizar metadata
```sql
-- Atualizar metadata do usuário
UPDATE auth.users
SET raw_user_meta_data = jsonb_set(
    raw_user_meta_data,
    '{display_name}',
    '"Novo Nome"'
)
WHERE email = 'admin@test.com';

-- Verificar se sincronizou
SELECT display_name FROM public.profiles WHERE email = 'admin@test.com';
```

### Teste 3: Deletar usuário
```sql
-- Deletar usuário
DELETE FROM auth.users WHERE email = 'admin@test.com';

-- Verificar se o profile foi deletado
SELECT * FROM public.profiles WHERE email = 'admin@test.com';
-- Deve retornar vazio
```

## 📝 Metadados Esperados

Ao criar usuários no Supabase, use esta estrutura de metadados:

```json
{
  "role": "USER",
  "display_name": "Nome do Usuário",
  "name": "Nome Alternativo"
}
```

- `role`: `"USER"` ou `"ADMIN"`
- `display_name`: Nome a ser exibido na aplicação
- `name`: Campo alternativo para nome

## 🔍 Troubleshooting

### Problema: Profile não é criado automaticamente
**Solução:**
```sql
-- Verificar se o trigger existe
SELECT * FROM pg_trigger WHERE tgname = 'on_auth_user_created';

-- Recriar trigger se necessário
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW
  EXECUTE FUNCTION public.handle_new_user();
```

### Problema: Erro de permissão no trigger
**Solução:**
```sql
-- Garantir que a função tem permissões corretas
ALTER FUNCTION public.handle_new_user() SECURITY DEFINER;
```

### Problema: RLS bloqueando acesso
**Solução:**
```sql
-- Desabilitar temporariamente RLS para debug
ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;

-- Reabilitar após correção
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
```

## 📚 Recursos Adicionais

- [Supabase Auth Documentation](https://supabase.com/docs/guides/auth)
- [Supabase Triggers Documentation](https://supabase.com/docs/guides/database/postgres/triggers)
- [Row Level Security Guide](https://supabase.com/docs/guides/auth/row-level-security)

## ✅ Checklist de Implementação

- [x] Migration V9 criada
- [x] Entidade User atualizada
- [x] UserRepository atualizado
- [x] UserService atualizado
- [x] Triggers criados
- [x] RLS policies implementadas
- [ ] Testar criação de usuário via Supabase
- [ ] Configurar frontend com Supabase SDK
- [ ] Implementar validação de JWT do Supabase (opcional)
- [ ] Documentar API endpoints
- [ ] Testes de integração

---

**Implementado por:** AI Assistant  
**Data:** 2025-10-22  
**Versão:** 1.0.0

