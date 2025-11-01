-- Create exams table
CREATE TABLE public.exams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(20) NOT NULL,
    institution VARCHAR(100),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Create unique constraint for slug
CREATE UNIQUE INDEX idx_exams_slug_unique ON public.exams(slug);

-- Create indexes for faster queries
CREATE INDEX idx_exams_institution ON public.exams(institution);
CREATE INDEX idx_exams_is_active ON public.exams(is_active);
