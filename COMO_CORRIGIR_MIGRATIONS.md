# 🔧 Como Corrigir o Erro das Migrations

## 🚨 Problema

Você tinha uma **V10** que já foi executada, mas as **V8** e **V9** são novas. O Flyway pula versões menores que já foram executadas, causando o erro:

```
ERROR: relation "public.exams" does not exist
```

## ✅ Solução (Escolha UMA das opções abaixo)

---

## 🎯 **OPÇÃO 1: Executar SQL Manual no Supabase (RECOMENDADO)**

Esta é a forma mais rápida e segura!

### Passo 1: Abrir o Supabase SQL Editor

1. Acesse https://supabase.com/dashboard
2. Selecione seu projeto
3. Vá em **SQL Editor**

### Passo 2: Executar o Script de Correção

Copie e cole TODO o conteúdo do arquivo **`fix_migrations.sql`** que acabei de criar no SQL Editor e execute.

O script vai:
- ✅ Criar a tabela `exams`
- ✅ Inserir 5 provas de exemplo
- ✅ Adicionar coluna `exam_id` em `questions`
- ✅ Criar foreign key e índices
- ✅ Registrar V8 e V9 no histórico do Flyway
- ✅ Atualizar V10 para V11

### Passo 3: Reiniciar a Aplicação

```bash
./gradlew bootRun
```

Pronto! Deve funcionar agora. ✨

---

## 🎯 **OPÇÃO 2: Limpar e Recriar (Se a OPÇÃO 1 não funcionar)**

⚠️ **CUIDADO**: Isso vai **apagar todo o histórico do Flyway**!

### Passo 1: No Supabase SQL Editor, execute:

```sql
-- Limpar histórico do Flyway
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
```

### Passo 2: Reiniciar a Aplicação

```bash
./gradlew clean bootRun
```

O Flyway vai recriar tudo do zero na ordem correta:
- V1, V2, V3, V4, V5, V6, V7, V8, V9, V11

---

## 🔍 Como Verificar se Funcionou

### No Supabase SQL Editor:

```sql
-- 1. Ver todas as migrations aplicadas
SELECT installed_rank, version, description, installed_on
FROM flyway_schema_history
ORDER BY installed_rank;

-- 2. Ver as provas criadas
SELECT id, name, exam_type, institution, year 
FROM public.exams;

-- 3. Verificar exam_id em questions
SELECT column_name, data_type 
FROM information_schema.columns
WHERE table_name = 'questions' AND column_name = 'exam_id';
```

### Na API (após reiniciar):

```bash
# Testar endpoint de provas
curl http://localhost:8080/api/exams/summary

# Deve retornar:
# [
#   {"id":1,"name":"ENEM 2023","examType":"ENEM",...},
#   {"id":2,"name":"ENEM 2022","examType":"ENEM",...},
#   ...
# ]
```

---

## 📝 O que Eu Já Fiz

✅ Renomeei `V10__setup_supabase_auth_integration.sql` para `V11__setup_supabase_auth_integration.sql`

Agora você só precisa executar **OPÇÃO 1** (SQL manual) ou **OPÇÃO 2** (limpar e recriar).

---

## 💡 Resumo Simplificado

```
1️⃣ Copie o conteúdo de fix_migrations.sql
2️⃣ Cole no Supabase SQL Editor
3️⃣ Execute
4️⃣ Reinicie: ./gradlew bootRun
5️⃣ Teste: curl http://localhost:8080/api/exams/summary
```

**Pronto! 🚀**

---

## 🆘 Se Ainda Tiver Problemas

Execute no Supabase e me envie o resultado:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

Assim posso te ajudar melhor!

