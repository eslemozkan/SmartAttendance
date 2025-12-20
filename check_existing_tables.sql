-- Check all existing tables in the database

-- 1. List all tables in public schema
SELECT 
    table_schema,
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;

-- 2. Check if course_class_assignments exists (case insensitive)
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND LOWER(table_name) LIKE '%course%class%' 
   OR LOWER(table_name) LIKE '%assignment%'
ORDER BY table_name;

-- 3. Check if courses table exists
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name = 'courses';

-- 4. Check if classes table exists
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name = 'classes';

-- 5. Check if students table exists
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name = 'students';

-- 6. Check all tables that might be related to course assignments
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND (
    LOWER(table_name) LIKE '%course%' OR
    LOWER(table_name) LIKE '%class%' OR
    LOWER(table_name) LIKE '%assignment%' OR
    LOWER(table_name) LIKE '%teacher%'
  )
ORDER BY table_name;

-- 7. Check teacher_courses table structure (if exists)
SELECT 
    column_name,
    data_type,
    is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'teacher_courses'
ORDER BY ordinal_position;

-- 8. Check if there's a view for course assignments
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_type = 'VIEW'
  AND (
    LOWER(table_name) LIKE '%course%' OR
    LOWER(table_name) LIKE '%assignment%'
  )
ORDER BY table_name;

-- 9. Sample data from teacher_courses (if exists)
SELECT 
    id,
    teacher_id,
    course_id,
    created_at
FROM public.teacher_courses
LIMIT 5;

-- 10. Sample data from courses (if exists)
SELECT 
    id,
    name,
    code,
    department_id
FROM public.courses
LIMIT 5;

-- 11. Sample data from students (if exists)
SELECT 
    id,
    email,
    class_id,
    full_name
FROM public.students
LIMIT 5;

-- 12. Check foreign key relationships to understand table structure
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
  AND tc.table_schema = 'public'
  AND (
    tc.table_name LIKE '%course%' OR
    tc.table_name LIKE '%class%' OR
    tc.table_name LIKE '%teacher%'
  )
ORDER BY tc.table_name;








