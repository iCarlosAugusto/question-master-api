-- Create question types enum
-- CREATE TYPE question_type AS ENUM ('MULTIPLE_CHOICE', 'TRUE_FALSE');

-- Create questions table
CREATE TABLE public.questions (
    id UUID PRIMARY KEY,
    statement TEXT NOT NULL,
    subject_id BIGINT NOT NULL REFERENCES public.subjects(id) ON DELETE RESTRICT,
    year SMALLINT,
    qtype VARCHAR(30) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create question-topics many-to-many relationship table
CREATE TABLE public.question_topics (
    question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES public.topics(id) ON DELETE CASCADE,
    PRIMARY KEY (question_id, topic_id)
);
