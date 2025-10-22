# 🔑 Como Obter o JWT Secret do Supabase

## ⚠️ IMPORTANTE: O que você configurou está ERRADO!

Você colocou:
```properties
supabase.jwt.secret=+zpR/X0Hgds89beVkgtFrnkqx8tzt0pJ/E0EE4emvHInj8XgHI8+AFR14eLzfoGBZ92vUe7TEwox6l8OCT8EMg==
```

Mas esse é o **secret da SUA API**, não do Supabase! Por isso o token não valida.

---

## 📋 Passo a Passo para Obter o JWT Secret CORRETO:

### **1. Acesse o Supabase Dashboard**
- URL: https://supabase.com/dashboard
- Faça login na sua conta

### **2. Selecione seu projeto**
- Clique no projeto: `swzkcdgnahprrfzpfaoi` (baseado na sua connection string)

### **3. Vá em Project Settings**
```
Dashboard → Settings (ícone de engrenagem) → Project Settings
```

### **4. Clique em "API" no menu lateral**
```
Project Settings → API
```

### **5. Procure pela seção "JWT Secret"**

Você verá algo assim:

```
┌────────────────────────────────────────────────────┐
│ Project API keys                                   │
├────────────────────────────────────────────────────┤
│                                                    │
│ anon public                                        │
│ eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...          │
│ ⚠️ NÃO É ESTE!                                     │
│                                                    │
│ service_role secret                                │
│ eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...          │
│ ⚠️ NÃO É ESTE!                                     │
│                                                    │
└────────────────────────────────────────────────────┘

Role para baixo até encontrar:

┌────────────────────────────────────────────────────┐
│ JWT Settings                                       │
├────────────────────────────────────────────────────┤
│                                                    │
│ JWT Secret (used to decode your JWTs)             │
│ your-super-secret-jwt-token-with-...              │
│ ✅ ESTE É O CORRETO!                               │
│                                                    │
│ [Copy] [Reveal]                                    │
│                                                    │
└────────────────────────────────────────────────────┘
```

### **6. Clique em "Reveal" e depois "Copy"**

O secret será algo como:
```
your-super-secret-jwt-token-with-at-least-32-characters-long
```

**NÃO** é um JWT (não começa com `eyJ`), é uma string simples!

---

## 🔧 Como Usar:

### **Opção 1: Adicionar direto no arquivo (desenvolvimento apenas)**

```properties
# application-dev.properties
supabase.jwt.secret=cole-aqui-o-secret-que-você-copiou
```

### **Opção 2: Usar variável de ambiente (recomendado)**

```bash
# No terminal, antes de rodar a aplicação:
export SUPABASE_JWT_SECRET="cole-aqui-o-secret-que-você-copiou"
./gradlew bootRun
```

E no arquivo:
```properties
# application-dev.properties
supabase.jwt.secret=${SUPABASE_JWT_SECRET:}
```

---

## 🧪 Como Testar se está Correto:

### **1. Decodifique seu token JWT do Supabase**

Vá em: https://jwt.io

Cole o token:
```
eyJhbGciOiJIUzI1NiIsImtpZCI6IitEbDR2VFI0UXdaaFhpRS8iLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL3N3emtjZGduYWhwcnJmenBmYW9pLnN1cGFiYXNlLmNvL2F1dGgvdjEiLCJzdWIiOiI3NDYwZGY1NS1mZTJmLTQ4ZmMtOGMxYi1kYjE2MGY4YTBmMGYi...
```

Você verá:
```json
{
  "iss": "https://swzkcdgnahprrfzpfaoi.supabase.co/auth/v1",
  "sub": "7460df55-fe2f-48fc-8c1b-db160f8a0f0f",
  "aud": "authenticated",
  "user_metadata": {
    "display_name": "senha123",
    "email": "senha123@gmail.com"
  }
}
```

### **2. Verifique se o usuário existe no banco**

```sql
SELECT * FROM public.profiles 
WHERE id = '7460df55-fe2f-48fc-8c1b-db160f8a0f0f';
```

**Se não retornar nada, crie o usuário:**

```sql
INSERT INTO public.profiles (id, role, display_name, email, created_at, updated_at)
VALUES (
  '7460df55-fe2f-48fc-8c1b-db160f8a0f0f',
  'USER',
  'senha123',
  'senha123@gmail.com',
  NOW(),
  NOW()
);
```

### **3. Reinicie a aplicação e teste novamente**

```bash
./gradlew bootRun
```

---

## 🔍 Verificação dos Logs:

Com os novos logs adicionados, você verá:

**✅ Se estiver funcionando:**
```
🔍 [JWT Filter] Request: POST /api/questions/.../answer
🔍 [JWT Filter] Has Authorization header: true
🔍 [JWT Filter] JWT extracted: Yes (eyJhbGciOiJIUzI1NiIs...)
🔍 [JWT Filter] Validating token...
🔍 [JWT Filter] Token valid: true
🔍 [JWT Filter] User ID from token: 7460df55-fe2f-48fc-8c1b-db160f8a0f0f
🔍 [JWT Filter] User loaded: 7460df55-fe2f-48fc-8c1b-db160f8a0f0f
🔍 [JWT Filter] Authorities: [ROLE_USER]
✅ [JWT Filter] Authentication set successfully!
```

**❌ Se o secret estiver errado:**
```
🔍 [JWT Filter] Request: POST /api/questions/.../answer
🔍 [JWT Filter] Has Authorization header: true
🔍 [JWT Filter] JWT extracted: Yes (eyJhbGciOiJIUzI1NiIs...)
🔍 [JWT Filter] Validating token...
JWT validation error: JWT signature does not match locally computed signature
⚠️ [JWT Filter] Token validation FAILED
```

**❌ Se o usuário não existir:**
```
🔍 [JWT Filter] Token valid: true
🔍 [JWT Filter] User ID from token: 7460df55-fe2f-48fc-8c1b-db160f8a0f0f
❌ [JWT Filter] Error: User not found with id: 7460df55-fe2f-48fc-8c1b-db160f8a0f0f
```

---

## 📞 Ainda com Problemas?

### Execute este comando e me envie a saída:

```bash
# Ver os logs em tempo real
tail -f logs/spring.log | grep "JWT Filter"
```

Ou se não tiver arquivo de log:
```bash
./gradlew bootRun 2>&1 | grep "JWT Filter"
```

---

## ✅ Checklist Final:

- [ ] Acessei o Supabase Dashboard
- [ ] Fui em Project Settings → API
- [ ] Copiei o **JWT Secret** (não o anon key!)
- [ ] Colei no `application-dev.properties` em `supabase.jwt.secret`
- [ ] Verifiquei que o usuário existe na tabela `profiles`
- [ ] Reiniciei a aplicação
- [ ] Testei a requisição novamente
- [ ] Verifiquei os logs com os emojis 🔍 ✅ ❌

---

**Importante**: O JWT Secret do Supabase é diferente para cada projeto!

