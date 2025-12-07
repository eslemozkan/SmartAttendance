-- Populate course_class_assignments from existing data
-- This creates course-class assignments based on:
-- 1. Courses with teacher_id -> assign to classes where teacher's students are
-- 2. teacher_courses -> assign to classes where teacher's students are
-- 3. Or assign all courses to all classes (for testing)

-- First, let's see what we have
SELECT 
    'Total courses' AS info,
    COUNT(*) AS count
FROM public.courses;

SELECT 
    'Total classes' AS info,
    COUNT(*) AS count
FROM public.classes;

SELECT 
    'Total students' AS info,
    COUNT(*) AS count
FROM public.students;

SELECT 
    'Courses with teacher_id' AS info,
    COUNT(*) AS count
FROM public.courses
WHERE teacher_id IS NOT NULL;

-- Strategy 1: Assign courses to classes based on teacher's students
-- If a teacher teaches a course and has students in a class, assign that course to that class
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
SELECT DISTINCT
    c.id AS course_id,
    s.class_id AS class_id,
    c.teacher_id,
    COALESCE(cl.academic_year, '2024-2025') AS academic_year,
    'Güz' AS semester
FROM public.courses c
JOIN public.students s ON s.class_id IS NOT NULL
JOIN public.classes cl ON cl.id = s.class_id
WHERE c.teacher_id IS NOT NULL
  AND s.class_id IS NOT NULL
  -- Only if there are students in that class
  AND EXISTS (
    SELECT 1 FROM public.students s2 
    WHERE s2.class_id = s.class_id
  )
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;

-- Strategy 2: Use teacher_courses table
-- If a teacher is assigned to a course via teacher_courses, 
-- assign that course to classes where that teacher's students are
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
SELECT DISTINCT
    tc.course_id AS course_id,
    s.class_id AS class_id,
    tc.teacher_id,
    COALESCE(cl.academic_year, '2024-2025') AS academic_year,
    'Güz' AS semester
FROM public.teacher_courses tc
JOIN public.students s ON s.class_id IS NOT NULL
JOIN public.classes cl ON cl.id = s.class_id
WHERE s.class_id IS NOT NULL
  -- Try to match teacher with students (if you have a way to link them)
  -- For now, assign to all classes (you can refine this)
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;

-- Strategy 3: Assign all courses to all classes (simple but creates many records)
-- Uncomment if you want to assign all courses to all classes
/*
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
SELECT DISTINCT
    c.id AS course_id,
    cl.id AS class_id,
    c.teacher_id,
    cl.academic_year,
    'Güz' AS semester
FROM public.courses c
CROSS JOIN public.classes cl
WHERE cl.academic_year = '2024-2025'
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;
*/

-- Check what was created
SELECT 
    'Total assignments created' AS info,
    COUNT(*) AS count
FROM public.course_class_assignments;

-- Show sample assignments
SELECT 
    cca.id,
    c.name AS course_name,
    c.code AS course_code,
    cl.name AS class_name,
    cl.academic_year,
    p.email AS teacher_email,
    p.full_name AS teacher_name,
    cca.semester,
    (SELECT COUNT(*) FROM public.students WHERE class_id = cl.id) AS student_count
FROM public.course_class_assignments cca
JOIN public.courses c ON c.id = cca.course_id
JOIN public.classes cl ON cl.id = cca.class_id
LEFT JOIN public.profiles p ON p.id = cca.teacher_id
ORDER BY cl.name, c.name
LIMIT 20;

-- Check assignments per class
SELECT 
    cl.name AS class_name,
    COUNT(DISTINCT cca.course_id) AS course_count,
    COUNT(DISTINCT (SELECT COUNT(*) FROM public.students WHERE class_id = cl.id)) AS student_count
FROM public.classes cl
LEFT JOIN public.course_class_assignments cca ON cca.class_id = cl.id
GROUP BY cl.id, cl.name
ORDER BY cl.name;

-- Check if a specific student's class has courses assigned
-- Replace 'student@example.com' with actual student email
SELECT 
    s.email AS student_email,
    s.full_name AS student_name,
    cl.name AS class_name,
    COUNT(DISTINCT cca.course_id) AS assigned_courses
FROM public.students s
JOIN public.classes cl ON cl.id = s.class_id
LEFT JOIN public.course_class_assignments cca ON cca.class_id = s.class_id
WHERE s.email = 'student@example.com'  -- Replace with actual email
GROUP BY s.id, s.email, s.full_name, cl.name;

