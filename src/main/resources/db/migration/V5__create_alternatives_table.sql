-- Create alternatives table
CREATE TABLE public.alternatives (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL DEFAULT FALSE,
    ord SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (question_id, ord)
);

-- Add generated column for label (A, B, C, ...)
ALTER TABLE public.alternatives
ADD COLUMN label TEXT GENERATED ALWAYS AS (
    chr(64 + ord) -- 65 = 'A'
) STORED;

-- Guarantee exactly one correct alternative per question
CREATE UNIQUE INDEX ux_alternatives_one_correct_per_question
    ON public.alternatives (question_id)
    WHERE is_correct;
