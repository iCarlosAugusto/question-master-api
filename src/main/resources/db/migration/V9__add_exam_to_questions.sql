-- Add exam_id column to questions table
ALTER TABLE questions ADD COLUMN exam_id BIGINT;

-- Add foreign key constraint
ALTER TABLE public.questions
ADD CONSTRAINT fk_questions_exam 
FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE SET NULL;

-- Create index for faster queries
CREATE INDEX idx_questions_exam_id ON public.questions(exam_id);


