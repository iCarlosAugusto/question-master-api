-- ============================================================================
-- Script para corrigir as migrations do Flyway
-- Execute este script no Supabase SQL Editor
-- ============================================================================

-- 1. Verificar o estado atual das migrations
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;

-- 2. Verificar se a tabela exams já existe
SELECT EXISTS (
    SELECT FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'exams'
) as exams_exists;

-- 3. Verificar se exam_id existe em questions
SELECT EXISTS (
    SELECT FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'questions' 
    AND column_name = 'exam_id'
) as exam_id_exists;

-- ============================================================================
-- SOLUÇÃO: Executar as migrations V8 e V9 manualmente e registrar no Flyway
-- ============================================================================

-- Passo 1: Criar tabela exams (V8)
CREATE TABLE IF NOT EXISTS public.exams (
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

-- Criar índices
CREATE INDEX IF NOT EXISTS idx_exams_exam_type ON public.exams(exam_type);
CREATE INDEX IF NOT EXISTS idx_exams_year ON public.exams(year);
CREATE INDEX IF NOT EXISTS idx_exams_institution ON public.exams(institution);
CREATE INDEX IF NOT EXISTS idx_exams_is_active ON public.exams(is_active);

-- Inserir dados de exemplo (se não existirem)
INSERT INTO public.exams (name, exam_type, institution, year, description) 
VALUES
    ('ENEM 2023', 'ENEM', 'INEP', 2023, 'Exame Nacional do Ensino Médio 2023'),
    ('ENEM 2022', 'ENEM', 'INEP', 2022, 'Exame Nacional do Ensino Médio 2022'),
    ('Concurso Público TRT 2023', 'CONCURSO', 'TRT', 2023, 'Tribunal Regional do Trabalho'),
    ('Vestibular FUVEST 2023', 'VESTIBULAR', 'FUVEST', 2023, 'Vestibular da USP'),
    ('OAB 1ª Fase 2023', 'CERTIFICACAO', 'OAB', 2023, 'Ordem dos Advogados do Brasil')
ON CONFLICT DO NOTHING;

-- Passo 2: Adicionar exam_id em questions (V9)
DO $$ 
BEGIN
    -- Adicionar coluna exam_id se não existir
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'questions' 
        AND column_name = 'exam_id'
    ) THEN
        ALTER TABLE public.questions ADD COLUMN exam_id BIGINT;
    END IF;
    
    -- Adicionar foreign key se não existir
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_schema = 'public' 
        AND table_name = 'questions' 
        AND constraint_name = 'fk_questions_exam'
    ) THEN
        ALTER TABLE public.questions
        ADD CONSTRAINT fk_questions_exam 
        FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE SET NULL;
    END IF;
END $$;

-- Criar índice
CREATE INDEX IF NOT EXISTS idx_questions_exam_id ON public.questions(exam_id);

-- Passo 3: Registrar as migrations V8 e V9 no Flyway
-- Nota: Ajuste o installed_rank conforme necessário

-- Registrar V8
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
    8,  -- installed_rank (ajuste se necessário)
    '8',
    'create exams table',
    'SQL',
    'V8__create_exams_table.sql',
    -1234567890,  -- checksum placeholder
    CURRENT_USER,
    NOW(),
    100,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '8'
);

-- Registrar V9
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
    9,  -- installed_rank (ajuste se necessário)
    '9',
    'add exam to questions',
    'SQL',
    'V9__add_exam_to_questions.sql',
    -987654321,  -- checksum placeholder
    CURRENT_USER,
    NOW(),
    100,
    true
WHERE NOT EXISTS (
    SELECT 1 FROM flyway_schema_history WHERE version = '9'
);

-- Passo 4: Atualizar o installed_rank da V10 (agora V11)
UPDATE flyway_schema_history 
SET version = '11',
    installed_rank = 11,
    script = 'V11__setup_supabase_auth_integration.sql'
WHERE version = '10' 
AND description = 'setup supabase auth integration';

-- ============================================================================
-- SOLUÇÃO ALTERNATIVA: Se houver erro de "policy already exists"
-- ============================================================================
-- Execute isso ANTES de reiniciar a aplicação:

DO $$ 
BEGIN
    -- Drop todas as policies existentes para evitar conflitos
    DROP POLICY IF EXISTS "Users can view own profile" ON public.profiles;
    DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
    DROP POLICY IF EXISTS "Service can insert profiles" ON public.profiles;
    DROP POLICY IF EXISTS "Admins can view all profiles" ON public.profiles;
    DROP POLICY IF EXISTS "Admins can update any profile" ON public.profiles;
    DROP POLICY IF EXISTS "Admins can delete profiles" ON public.profiles;
    
    -- Drop e recria os triggers para garantir que estão corretos
    DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
    DROP TRIGGER IF EXISTS on_auth_user_updated ON auth.users;
    DROP TRIGGER IF EXISTS on_auth_user_deleted ON auth.users;
    
    RAISE NOTICE 'Policies e triggers limpos com sucesso!';
END $$;

-- ============================================================================
-- Verificação Final
-- ============================================================================

-- Verificar as migrations registradas
SELECT installed_rank, version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;

-- Verificar se exams foi criada
SELECT COUNT(*) as exam_count FROM public.exams;

-- Verificar se exam_id existe em questions
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
AND table_name = 'questions' 
AND column_name = 'exam_id';

-- ============================================================================
-- Pronto! Agora você pode reiniciar a aplicação Spring Boot
-- ============================================================================

