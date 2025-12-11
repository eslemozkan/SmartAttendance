-- Add grade_level column to classes table
-- This allows assigning courses to all classes of a specific grade level (1. sınıf, 2. sınıf, etc.)

-- 1. Add grade_level column
ALTER TABLE public.classes 
ADD COLUMN IF NOT EXISTS grade_level INTEGER;

-- 2. Extract grade level from class name (1-A -> 1, 2-B -> 2, etc.)
UPDATE public.classes
SET grade_level = CAST(SUBSTRING(name FROM '^(\d+)') AS INTEGER)
WHERE grade_level IS NULL AND name ~ '^\d+';

-- 3. Create index for better performance
CREATE INDEX IF NOT EXISTS idx_classes_grade_level ON public.classes(grade_level);

-- 4. Add comment
COMMENT ON COLUMN public.classes.grade_level IS 'Sınıf seviyesi (1, 2, 3, 4) - 1. sınıf, 2. sınıf gibi';



