-- Check and fix grade_level for classes
-- Run this in Supabase SQL Editor

-- 1. Check if grade_level column exists
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND table_name = 'classes' 
  AND column_name = 'grade_level';

-- 2. Add grade_level column if it doesn't exist
ALTER TABLE public.classes 
ADD COLUMN IF NOT EXISTS grade_level INTEGER;

-- 3. Update grade_level from class name (1-A -> 1, 2-B -> 2, etc.)
UPDATE public.classes
SET grade_level = CAST(SUBSTRING(name FROM '^(\d+)') AS INTEGER)
WHERE grade_level IS NULL 
  AND name ~ '^\d+';

-- 4. For classes that don't match the pattern, set to NULL or handle manually
-- Example: If you have "Mezun" or other names, you might want to handle them differently

-- 5. Verify the update
SELECT name, grade_level, department_id, academic_year
FROM public.classes
ORDER BY grade_level, name
LIMIT 20;

-- 6. Create index for better performance
CREATE INDEX IF NOT EXISTS idx_classes_grade_level ON public.classes(grade_level);



