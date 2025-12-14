-- Fix course_class_assignments table permissions and RLS for mobile app access
-- This allows students to query their courses through the API

-- 1. Disable RLS temporarily to fix permissions
ALTER TABLE public.course_class_assignments DISABLE ROW LEVEL SECURITY;

-- 2. Drop existing policies
DROP POLICY IF EXISTS "Admins can manage course assignments" ON public.course_class_assignments;

-- 3. Grant SELECT permissions to anon and authenticated roles
GRANT SELECT ON public.course_class_assignments TO anon, authenticated;

-- 4. Check data type compatibility
-- If course_class_assignments.course_id is UUID but courses.id is BIGINT, we need to handle this
DO $$
DECLARE
    cca_course_id_type TEXT;
    courses_id_type TEXT;
BEGIN
    -- Get data types
    SELECT data_type INTO cca_course_id_type
    FROM information_schema.columns
    WHERE table_schema = 'public' 
      AND table_name = 'course_class_assignments' 
      AND column_name = 'course_id';
    
    SELECT data_type INTO courses_id_type
    FROM information_schema.columns
    WHERE table_schema = 'public' 
      AND table_name = 'courses' 
      AND column_name = 'id';
    
    -- Log the types (for debugging)
    RAISE NOTICE 'course_class_assignments.course_id type: %', cca_course_id_type;
    RAISE NOTICE 'courses.id type: %', courses_id_type;
    
    -- If types don't match, we might need to create a view or handle it in the API
    -- For now, we'll just ensure permissions are correct
END $$;

-- 5. Create a view that handles type conversion if needed
-- This view will be used by the mobile app instead of direct table access
DROP VIEW IF EXISTS public.student_course_assignments CASCADE;

CREATE OR REPLACE VIEW public.student_course_assignments AS
SELECT 
    cca.id AS assignment_id,
    cca.class_id,
    cca.teacher_id,
    cca.academic_year,
    cca.semester,
    -- Handle both UUID and BIGINT course_id types
    CASE 
        WHEN pg_typeof(cca.course_id)::text = 'uuid' THEN 
            -- If course_id is UUID, try to find matching BIGINT course
            (SELECT id::text FROM public.courses WHERE id::text = cca.course_id::text LIMIT 1)
        ELSE 
            cca.course_id::text
    END AS course_id,
    c.id AS course_id_bigint,
    c.name AS course_name,
    c.code AS course_code
FROM public.course_class_assignments cca
LEFT JOIN public.courses c ON 
    CASE 
        WHEN pg_typeof(cca.course_id)::text = 'uuid' THEN 
            c.id::text = cca.course_id::text
        ELSE 
            c.id = cca.course_id::bigint
    END;

-- 6. Grant permissions on the view
GRANT SELECT ON public.student_course_assignments TO anon, authenticated;

-- 7. Also ensure courses table is accessible
ALTER TABLE public.courses DISABLE ROW LEVEL SECURITY;
GRANT SELECT ON public.courses TO anon, authenticated;

-- 8. Ensure classes table is accessible (needed for class_id lookups)
ALTER TABLE public.classes DISABLE ROW LEVEL SECURITY;
GRANT SELECT ON public.classes TO anon, authenticated;

-- 9. Notify PostgREST to reload schema
NOTIFY pgrst, 'reload schema';

-- 10. Verify permissions
SELECT 
    grantee,
    table_name,
    privilege_type
FROM information_schema.role_table_grants
WHERE table_schema = 'public'
  AND table_name IN ('course_class_assignments', 'courses', 'classes', 'student_course_assignments')
  AND grantee IN ('anon', 'authenticated')
ORDER BY table_name, grantee;






