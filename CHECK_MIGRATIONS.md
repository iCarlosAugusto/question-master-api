# 🔍 Como Verificar e Resolver o Erro "relation exams does not exist"

## 🚨 Problema

Você está recebendo o erro:
```
ERROR: relation "exams" does not exist
```

Isso significa que as migrations **V8** e **V9** ainda não foram executadas no banco de dados.

---

## ✅ Solução 1: Deixar o Spring Boot executar as migrations automaticamente

O Spring Boot, com Flyway configurado, executa as migrations automaticamente quando a aplicação inicia.

### Passos:

1. **Parar a aplicação** (se estiver rodando):
```bash
pkill -f "gradlew bootRun"
```

2. **Limpar o build** (opcional, mas recomendado):
```bash
./gradlew clean build
```

3. **Iniciar a aplicação**:
```bash
./gradlew bootRun
```

4. **Verificar os logs**:
Você deve ver algo como:
```
INFO  org.flywaydb.core.FlywayExecutor : Database: jdbc:postgresql://...
INFO  o.f.core.internal.command.DbMigrate : Current version of schema "public": 7
INFO  o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "8 - create exams table"
INFO  o.f.core.internal.command.DbMigrate : Migrating schema "public" to version "9 - add exam to questions"
INFO  o.f.core.internal.command.DbMigrate : Successfully applied 2 migrations to schema "public"
```

---

## ✅ Solução 2: Executar as migrations manualmente no Supabase

Se preferir executar manualmente, acesse o **Supabase SQL Editor** e execute:

### 1. Criar tabela exams (V8)

```sql
-- Create exams table
CREATE TABLE IF NOT EXISTS exams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    exam_type VARCHAR(50) NOT NULL,
    institution VARCHAR(100),
    year SMALLINT,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_exams_exam_type ON exams(exam_type);
CREATE INDEX IF NOT EXISTS idx_exams_year ON exams(year);
CREATE INDEX IF NOT EXISTS idx_exams_institution ON exams(institution);
CREATE INDEX IF NOT EXISTS idx_exams_is_active ON exams(is_active);

-- Insert example exams
INSERT INTO exams (name, exam_type, institution, year, description) VALUES
('ENEM 2023', 'ENEM', 'INEP', 2023, 'Exame Nacional do Ensino Médio 2023'),
('ENEM 2022', 'ENEM', 'INEP', 2022, 'Exame Nacional do Ensino Médio 2022'),
('Concurso Público TRT 2023', 'CONCURSO', 'TRT', 2023, 'Tribunal Regional do Trabalho'),
('Vestibular FUVEST 2023', 'VESTIBULAR', 'FUVEST', 2023, 'Vestibular da USP'),
('OAB 1ª Fase 2023', 'CERTIFICACAO', 'OAB', 2023, 'Ordem dos Advogados do Brasil')
ON CONFLICT DO NOTHING;
```

### 2. Adicionar exam_id em questions (V9)

```sql
-- Add exam_id column to questions table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'questions' AND column_name = 'exam_id'
    ) THEN
        ALTER TABLE questions ADD COLUMN exam_id BIGINT;
    END IF;
END $$;

-- Add foreign key constraint (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_questions_exam'
    ) THEN
        ALTER TABLE questions 
        ADD CONSTRAINT fk_questions_exam 
        FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Create index (if not exists)
CREATE INDEX IF NOT EXISTS idx_questions_exam_id ON questions(exam_id);
```

### 3. Registrar as migrations no Flyway (IMPORTANTE!)

Após executar manualmente, você precisa registrar as migrations na tabela do Flyway para que ele não tente executá-las novamente:

```sql
-- Verificar a versão atual
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

-- Inserir registro da V8 (ajuste o checksum se necessário)
INSERT INTO flyway_schema_history (
    installed_rank, 
    version, 
    description, 
    type, 
    script, 
    checksum, 
    installed_by, 
    installed_on, 
    execution_time, 
    success
)
SELECT 
    COALESCE(MAX(installed_rank), 0) + 1,
    '8',
    'create exams table',
    'SQL',
    'V8__create_exams_table.sql',
    -1234567890,  -- checksum placeholder
    CURRENT_USER,
    NOW(),
    100,
    true
FROM flyway_schema_history
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '8'
);

-- Inserir registro da V9 (ajuste o checksum se necessário)
INSERT INTO flyway_schema_history (
    installed_rank, 
    version, 
    description, 
    type, 
    script, 
    checksum, 
    installed_by, 
    installed_on, 
    execution_time, 
    success
)
SELECT 
    COALESCE(MAX(installed_rank), 0) + 1,
    '9',
    'add exam to questions',
    'SQL',
    'V9__add_exam_to_questions.sql',
    -987654321,  -- checksum placeholder
    CURRENT_USER,
    NOW(),
    100,
    true
FROM flyway_schema_history
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '9'
);
```

---

## 🔍 Como Verificar se Funcionou

### 1. Verificar se a tabela existe:
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name = 'exams';
```

### 2. Verificar se há dados:
```sql
SELECT * FROM exams;
```

### 3. Verificar se exam_id foi adicionado em questions:
```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'questions' 
AND column_name = 'exam_id';
```

### 4. Verificar as migrations aplicadas:
```sql
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank DESC;
```

---

## 🧪 Testar a API

Após resolver, teste:

```bash
# 1. Listar provas
curl http://localhost:8080/api/exams/summary

# 2. Buscar questões (deve funcionar agora)
curl -H "X-Exam-Id: 1" http://localhost:8080/api/questions
```

---

## 🚨 Se Nada Funcionar (Última Opção)

Se tudo falhar, você pode limpar e recriar o schema do Flyway:

```sql
-- CUIDADO: Isso vai limpar o histórico do Flyway!
DROP TABLE IF EXISTS flyway_schema_history CASCADE;

-- Reinicie a aplicação e o Flyway vai recriar tudo do zero
```

Depois reinicie a aplicação:
```bash
./gradlew clean bootRun
```

---

## 📝 Resumo

**Opção mais simples**: Reinicie a aplicação Spring Boot e deixe o Flyway fazer seu trabalho.

**Se precisar fazer manual**: Execute os SQLs no Supabase SQL Editor e registre no Flyway.

**Verificação**: Use as queries de verificação para confirmar que tudo está OK.

---

**Escolha a Solução 1 (automática) se possível!** 🚀

