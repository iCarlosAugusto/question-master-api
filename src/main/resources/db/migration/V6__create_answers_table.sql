-- Create answers table
CREATE TABLE public.answers (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES public.questions(id) ON DELETE CASCADE,
    alternative_id UUID NOT NULL REFERENCES public.alternatives(id) ON DELETE RESTRICT,
    is_correct BOOLEAN NOT NULL,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
