-- Create course_class_assignments table if it doesn't exist
-- This table links courses to classes (which courses are taught to which classes)

-- 1. First check if table exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'course_class_assignments'
    ) THEN
        -- Create the table
        CREATE TABLE public.course_class_assignments (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            course_id BIGINT REFERENCES public.courses(id) ON DELETE CASCADE,
            class_id UUID REFERENCES public.classes(id) ON DELETE CASCADE,
            teacher_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
            academic_year TEXT NOT NULL DEFAULT '2024-2025',
            semester TEXT NOT NULL DEFAULT 'Güz',
            created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
            UNIQUE(course_id, class_id, academic_year, semester)
        );

        -- Create indexes
        CREATE INDEX IF NOT EXISTS idx_course_class_assignments_course_id 
            ON public.course_class_assignments(course_id);
        CREATE INDEX IF NOT EXISTS idx_course_class_assignments_class_id 
            ON public.course_class_assignments(class_id);
        CREATE INDEX IF NOT EXISTS idx_course_class_assignments_teacher_id 
            ON public.course_class_assignments(teacher_id);

        RAISE NOTICE 'course_class_assignments table created';
    ELSE
        RAISE NOTICE 'course_class_assignments table already exists';
    END IF;
END $$;

-- 2. Disable RLS for development
ALTER TABLE public.course_class_assignments DISABLE ROW LEVEL SECURITY;

-- 3. Drop existing policies if any
DROP POLICY IF EXISTS "Admins can manage course assignments" ON public.course_class_assignments;

-- 4. Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.course_class_assignments TO anon, authenticated;

-- 5. Verify table structure
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'course_class_assignments'
ORDER BY ordinal_position;

-- 6. Check if courses.id is BIGINT or UUID
SELECT 
    'courses.id type' AS info,
    data_type AS value
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'courses' 
  AND column_name = 'id';

-- 7. If courses.id is UUID but we created course_class_assignments with BIGINT,
--    we need to fix the foreign key
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
        -- Drop the foreign key constraint
        ALTER TABLE public.course_class_assignments 
            DROP CONSTRAINT IF EXISTS course_class_assignments_course_id_fkey;
        
        -- Drop the column
        ALTER TABLE public.course_class_assignments 
            DROP COLUMN IF EXISTS course_id;
        
        -- Recreate as UUID
        ALTER TABLE public.course_class_assignments 
            ADD COLUMN course_id UUID REFERENCES public.courses(id) ON DELETE CASCADE;
        
        -- Recreate index
        CREATE INDEX IF NOT EXISTS idx_course_class_assignments_course_id 
            ON public.course_class_assignments(course_id);
        
        RAISE NOTICE 'course_id column recreated as UUID to match courses.id';
    ELSE
        RAISE NOTICE 'course_id is already compatible with courses.id (type: %)', courses_id_type;
    END IF;
END $$;

-- 8. Notify PostgREST
NOTIFY pgrst, 'reload schema';

-- 9. Show final table structure
SELECT 
    'Final table structure' AS info,
    column_name,
    data_type
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'course_class_assignments'
ORDER BY ordinal_position;








