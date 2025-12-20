-- Simple approach: Assign all courses to all classes
-- This is useful for testing and ensures all students can see all courses
-- WARNING: This creates many records if you have many courses and classes

-- Check counts first
SELECT 
    (SELECT COUNT(*) FROM public.courses) AS total_courses,
    (SELECT COUNT(*) FROM public.classes) AS total_classes,
    (SELECT COUNT(*) FROM public.courses) * (SELECT COUNT(*) FROM public.classes) AS total_assignments_will_be_created;

-- Assign all courses to all classes for current academic year
INSERT INTO public.course_class_assignments (course_id, class_id, teacher_id, academic_year, semester)
SELECT DISTINCT
    c.id AS course_id,
    cl.id AS class_id,
    c.teacher_id,
    COALESCE(cl.academic_year, '2024-2025') AS academic_year,
    'Güz' AS semester
FROM public.courses c
CROSS JOIN public.classes cl
WHERE cl.academic_year = '2024-2025' OR cl.academic_year IS NULL
ON CONFLICT (course_id, class_id, academic_year, semester) DO NOTHING;

-- Verify
SELECT 
    'Assignments created' AS info,
    COUNT(*) AS count
FROM public.course_class_assignments;

-- Show sample
SELECT 
    c.name AS course_name,
    c.code AS course_code,
    cl.name AS class_name,
    p.email AS teacher_email
FROM public.course_class_assignments cca
JOIN public.courses c ON c.id = cca.course_id
JOIN public.classes cl ON cl.id = cca.class_id
LEFT JOIN public.profiles p ON p.id = cca.teacher_id
ORDER BY cl.name, c.name
LIMIT 10;








