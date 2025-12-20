-- Create course_class_assignments table
-- This links courses to classes (which courses are taught to which classes)

-- 1. Check courses.id data type first
SELECT 
    'courses.id type' AS info,
    data_type AS value
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'courses' 
  AND column_name = 'id';

-- 2. Create course_class_assignments table
-- We'll use the same type as courses.id
CREATE TABLE IF NOT EXISTS public.course_class_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id BIGINT REFERENCES public.courses(id) ON DELETE CASCADE,
    class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
    teacher_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    academic_year TEXT NOT NULL DEFAULT '2024-2025',
    semester TEXT NOT NULL DEFAULT 'Güz',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(course_id, class_id, academic_year, semester)
);

-- 3. If courses.id is UUID, we need to fix the foreign key
DO $$
DECLARE
    courses_id_type TEXT;
BEGIN
    SELECT data_type INTO courses_id_type
    FROM information_schema.columns
    WHERE table_schema = 'public' 
      AND table_name = 'courses' 
      AND column_name = 'id';
    
    IF courses_id_type = 'uuid' THEN
        -- Drop the table and recreate with UUID
        DROP TABLE IF EXISTS public.course_class_assignments CASCADE;
        
        CREATE TABLE public.course_class_assignments (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            course_id UUID REFERENCES public.courses(id) ON DELETE CASCADE,
            class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
            teacher_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
            academic_year TEXT NOT NULL DEFAULT '2024-2025',
            semester TEXT NOT NULL DEFAULT 'Güz',
            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
            UNIQUE(course_id, class_id, academic_year, semester)
        );
        
        RAISE NOTICE 'course_class_assignments created with UUID course_id';
    ELSE
        RAISE NOTICE 'course_class_assignments created with BIGINT course_id';
    END IF;
END $$;

-- 4. Create indexes
CREATE INDEX IF NOT EXISTS idx_course_class_assignments_course_id 
    ON public.course_class_assignments(course_id);
CREATE INDEX IF NOT EXISTS idx_course_class_assignments_class_id 
    ON public.course_class_assignments(class_id);
CREATE INDEX IF NOT EXISTS idx_course_class_assignments_teacher_id 
    ON public.course_class_assignments(teacher_id);

-- 5. Disable RLS for development
ALTER TABLE public.course_class_assignments DISABLE ROW LEVEL SECURITY;

-- 6. Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.course_class_assignments TO anon, authenticated;

-- 7. Notify PostgREST
NOTIFY pgrst, 'reload schema';

-- 8. Verify table structure
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'course_class_assignments'
ORDER BY ordinal_position;

-- 9. Show sample (should be empty initially)
SELECT COUNT(*) AS total_assignments FROM public.course_class_assignments;








