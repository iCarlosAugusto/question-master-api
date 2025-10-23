# 🔥 Solução Rápida para "policy already exists"

## 🚨 O Problema

Você tem policies do RLS que já existem no banco, causando conflito ao executar a migration V10/V11.

---

## ✅ SOLUÇÃO RÁPIDA (Execute no Supabase SQL Editor)

### Opção 1: Limpar Policies e Deixar Flyway Gerenciar ⚡ **RECOMENDADO**

```sql
-- 1. Limpar todas as policies existentes
DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
DROP POLICY IF EXISTS "Service can insert profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can update any profile" ON public.profiles;
DROP POLICY IF EXISTS "Admins can delete profiles" ON public.profiles;

-- 2. Limpar triggers existentes
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP TRIGGER IF EXISTS on_auth_user_updated ON auth.users;
DROP TRIGGER IF EXISTS on_auth_user_deleted ON auth.users;

-- 3. Marcar V10 como não executada no Flyway (para forçar re-execução)
DELETE FROM flyway_schema_history WHERE version = '10';

-- Pronto! Agora reinicie a aplicação
```

Depois:
```bash
./gradlew bootRun
```

O Flyway vai executar V10 (ou V11) novamente e criar tudo corretamente! ✨

---

## 💡 Opção 2: Executar Script Completo de Correção

Se preferir fazer tudo de uma vez (exams + supabase auth + correção):

```bash
# No terminal
cd /home/t.carlos.vieira/Desktop/DEV/personal-projects/pergutados/question-master-api
```

No **Supabase SQL Editor**, copie e execute **TODO** o conteúdo do arquivo `fix_migrations.sql` (que acabei de atualizar).

Ele vai:
- ✅ Criar tabela `exams`
- ✅ Adicionar `exam_id` em `questions`
- ✅ Limpar policies conflitantes
- ✅ Registrar V8, V9 e ajustar V10→V11 no Flyway

---

## 🔍 Como Saber se Funcionou?

### Teste 1: Ver migrations aplicadas
```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

**Esperado**: V1, V2, V3, V4, V5, V6, V7, V8, V9, V10 (ou V11)

### Teste 2: Ver policies criadas
```sql
SELECT tablename, policyname 
FROM pg_policies 
WHERE tablename = 'profiles';
```

**Esperado**: 6 policies listadas

### Teste 3: Ver triggers criados
```sql
SELECT trigger_name, event_object_table
FROM information_schema.triggers
WHERE trigger_name LIKE 'on_auth_user%';
```

**Esperado**: 3 triggers (created, updated, deleted)

---

## 🎯 Resumo Simplificado

**Execute no Supabase:**
```sql
DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
DROP POLICY IF EXISTS "Service can insert profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
DROP POLICY IF EXISTS "Admins can update any profile" ON public.profiles;
DROP POLICY IF EXISTS "Admins can delete profiles" ON public.profiles;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
DROP TRIGGER IF EXISTS on_auth_user_updated ON auth.users;
DROP TRIGGER IF EXISTS on_auth_user_deleted ON auth.users;

DELETE FROM flyway_schema_history WHERE version = '10';
```

**Execute no terminal:**
```bash
./gradlew bootRun
```

**Pronto! 🚀**

---

## 🔄 O que Eu Já Fiz por Você

✅ Atualizei a V10 para ser **idempotente** (com `DROP POLICY IF EXISTS`)  
✅ Criei V11 como cópia da V10 atualizada  
✅ Atualizei o `fix_migrations.sql` com a solução de limpeza de policies

Agora é só executar a Opção 1 acima! 😊

