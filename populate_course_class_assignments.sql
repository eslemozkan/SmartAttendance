-- Populate course_class_assignments table with initial data
-- This creates assignments based on existing courses and classes

-- Option 1: Assign all courses to all classes (for testing)
-- WARNING: This creates many records. Use with caution.

-- First, let's see what we have
SELECT 
    'Total courses' AS info,
    COUNT(*) AS count
FROM public.courses;

SELECT 
    'Total classes' AS info,
    COUNT(*) AS count
FROM public.classes;

-- Option 2: Assign courses to classes based on teacher assignments
-- If a teacher teaches a course, assign it to classes in their department
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
SELECT DISTINCT
    c.id AS course_id,
    cl.id AS class_id,
    c.teacher_id,
    cl.academic_year,
    'Güz' AS semester
FROM public.courses c
CROSS JOIN public.classes cl
WHERE c.teacher_id IS NOT NULL
  AND cl.academic_year = '2024-2025'
  -- Only assign if teacher's department matches class's department (if applicable)
  AND (
    c.department_id = cl.department_id 
    OR c.department_id IS NULL 
    OR cl.department_id IS NULL
  )
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;

-- Option 3: Manual assignment (recommended)
-- You can manually insert specific course-class assignments:
/*
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
VALUES 
    ((SELECT id FROM courses WHERE code = 'BM301' LIMIT 1), 
     (SELECT id FROM classes WHERE name = '1-A' LIMIT 1),
     (SELECT id FROM profiles WHERE email = 'teacher@example.com' LIMIT 1),
     '2024-2025', 'Güz'),
    -- Add more assignments as needed
    ...
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;
*/

-- Check what was created
SELECT 
    cca.id,
    c.name AS course_name,
    c.code AS course_code,
    cl.name AS class_name,
    p.email AS teacher_email,
    cca.academic_year,
    cca.semester
FROM public.course_class_assignments cca
JOIN public.courses c ON c.id = cca.course_id
JOIN public.classes cl ON cl.id = cca.class_id
LEFT JOIN public.profiles p ON p.id = cca.teacher_id
ORDER BY cl.name, c.name
LIMIT 20;


