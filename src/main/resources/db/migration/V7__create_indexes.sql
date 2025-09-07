-- Create indexes for better query performance

-- Query helpers for user/question status filters
CREATE INDEX ix_answers_user_question ON public.answers (user_id, question_id);
CREATE INDEX ix_answers_user_correct ON public.answers (user_id, is_correct);
CREATE INDEX ix_answers_question ON public.answers (question_id, answered_at DESC);

-- Question listing filters
CREATE INDEX ix_questions_subject_year ON public.questions (subject_id, year);
CREATE INDEX ix_questions_qtype ON public.questions (qtype);
CREATE INDEX ix_questions_active ON public.questions (is_active);

-- Text search on statement (requires pg_trgm extension)
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX ix_questions_statement_trgm ON public.questions USING gin (statement gin_trgm_ops);

-- Stable ordering for pagination
CREATE INDEX ix_questions_created_at_id ON public.questions (created_at DESC, id);

-- Additional useful indexes
CREATE INDEX ix_topics_subject_id ON public.topics (subject_id);
CREATE INDEX ix_alternatives_question_id ON public.alternatives (question_id, ord);
CREATE INDEX ix_question_topics_question_id ON public.question_topics (question_id);
CREATE INDEX ix_question_topics_topic_id ON public.question_topics (topic_id);
