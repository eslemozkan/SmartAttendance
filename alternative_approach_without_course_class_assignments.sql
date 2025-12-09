-- Alternative approach: If course_class_assignments doesn't exist,
-- we can use teacher_courses + students.class_id to find courses

-- This view creates a relationship between students and courses
-- based on teacher_courses and students.class_id
-- (assuming teachers teach courses to specific classes)

DROP VIEW IF EXISTS public.student_courses_view CASCADE;

CREATE OR REPLACE VIEW public.student_courses_view AS
SELECT DISTINCT
    s.id AS student_id,
    s.email AS student_email,
    s.class_id AS student_class_id,
    c.id AS course_id,
    c.name AS course_name,
    c.code AS course_code,
    tc.teacher_id,
    p.email AS teacher_email
FROM public.students s
CROSS JOIN public.teacher_courses tc
JOIN public.courses c ON c.id = tc.course_id
LEFT JOIN public.profiles p ON p.id = tc.teacher_id
WHERE s.class_id IS NOT NULL;

-- Grant permissions
GRANT SELECT ON public.student_courses_view TO anon, authenticated;

-- Alternative: More specific view if you have a way to link teachers to classes
-- (e.g., if teachers are assigned to specific classes)
DROP VIEW IF EXISTS public.student_courses_by_class CASCADE;

CREATE OR REPLACE VIEW public.student_courses_by_class AS
SELECT 
    s.id AS student_id,
    s.email AS student_email,
    s.class_id,
    c.id AS course_id,
    c.name AS course_name,
    c.code AS course_code
FROM public.students s
JOIN public.teacher_courses tc ON 1=1  -- This is a placeholder - adjust based on your logic
JOIN public.courses c ON c.id = tc.course_id
WHERE s.class_id IS NOT NULL;

-- Grant permissions
GRANT SELECT ON public.student_courses_by_class TO anon, authenticated;

-- Notify PostgREST
NOTIFY pgrst, 'reload schema';


