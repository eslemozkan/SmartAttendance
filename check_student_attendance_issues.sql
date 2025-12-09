-- Diagnostic queries to identify student attendance API issues

-- 1. Check course_class_assignments table structure and RLS
SELECT 
    'course_class_assignments' AS table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'course_class_assignments'
ORDER BY ordinal_position;

-- 2. Check RLS status for course_class_assignments
SELECT 
    schemaname,
    tablename,
    policyname,
    permissive,
    roles,
    cmd,
    qual,
    with_check
FROM pg_policies
WHERE schemaname = 'public' 
  AND tablename = 'course_class_assignments';

-- 3. Check if RLS is enabled
SELECT 
    relname AS table_name,
    relrowsecurity AS rls_enabled
FROM pg_class
WHERE relname = 'course_class_assignments';

-- 4. Check courses table structure
SELECT 
    'courses' AS table_name,
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'courses'
ORDER BY ordinal_position;

-- 5. Check data type compatibility between course_class_assignments.course_id and courses.id
SELECT 
    (SELECT data_type FROM information_schema.columns 
     WHERE table_name = 'course_class_assignments' AND column_name = 'course_id') AS cca_course_id_type,
    (SELECT data_type FROM information_schema.columns 
     WHERE table_name = 'courses' AND column_name = 'id') AS courses_id_type,
    CASE 
        WHEN (SELECT data_type FROM information_schema.columns 
              WHERE table_name = 'course_class_assignments' AND column_name = 'course_id') = 
             (SELECT data_type FROM information_schema.columns 
              WHERE table_name = 'courses' AND column_name = 'id')
        THEN 'MATCH' 
        ELSE 'MISMATCH' 
    END AS type_compatibility;

-- 6. Check sample data from course_class_assignments
SELECT 
    id,
    course_id,
    class_id,
    teacher_id,
    academic_year,
    semester
FROM public.course_class_assignments
LIMIT 5;

-- 7. Check sample data from courses
SELECT 
    id,
    name,
    code,
    department_id
FROM public.courses
LIMIT 5;

-- 8. Check if there are any course_class_assignments for a specific class
-- (Replace with actual class_id from your database)
SELECT 
    cca.id AS assignment_id,
    cca.course_id AS cca_course_id,
    cca.class_id,
    c.id AS course_id_from_courses,
    c.name AS course_name,
    c.code AS course_code
FROM public.course_class_assignments cca
LEFT JOIN public.courses c ON c.id::text = cca.course_id::text
LIMIT 10;

-- 9. Check permissions for anon and authenticated roles
SELECT 
    grantee,
    table_name,
    privilege_type
FROM information_schema.role_table_grants
WHERE table_schema = 'public'
  AND table_name IN ('course_class_assignments', 'courses')
  AND grantee IN ('anon', 'authenticated', 'public')
ORDER BY table_name, grantee;

-- 10. Check if there's a foreign key constraint
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'course_class_assignments'
  AND kcu.column_name = 'course_id';

-- 11. Count records in course_class_assignments
SELECT COUNT(*) AS total_assignments FROM public.course_class_assignments;

-- 12. Count records in courses
SELECT COUNT(*) AS total_courses FROM public.courses;

-- 13. Check if students table has class_id
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'students'
  AND column_name = 'class_id';

-- 14. Sample student data with class_id
SELECT 
    id,
    email,
    class_id,
    full_name
FROM public.students
LIMIT 5;


