-- Add exam_id column to subjects table (One-to-Many: Exam -> Subjects)
ALTER TABLE public.subjects 
ADD COLUMN exam_id BIGINT;

-- Add foreign key constraint
ALTER TABLE public.subjects
ADD CONSTRAINT fk_subjects_exam 
FOREIGN KEY (exam_id) REFERENCES public.exams(id) ON DELETE SET NULL;

-- Create index for faster queries
CREATE INDEX idx_subjects_exam_id ON public.subjects(exam_id);

