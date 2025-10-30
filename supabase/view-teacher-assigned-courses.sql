-- View: teacher_assigned_courses
-- Joins teacher_courses with courses and profiles to expose assigned courses per teacher

CREATE OR REPLACE VIEW public.teacher_assigned_courses AS
SELECT 
  tc.id AS assignment_id,
  p.id AS teacher_profile_id,
  p.email AS teacher_email,
  p.full_name AS teacher_name,
  c.id AS course_id,
  c.name AS course_name,
  c.code AS course_code,
  c.department_id
FROM public.teacher_courses tc
JOIN public.profiles p ON p.id = tc.teacher_id
JOIN public.courses c ON c.id = tc.course_id;

-- Dev: allow open reads
GRANT SELECT ON public.teacher_assigned_courses TO anon, authenticated;

NOTIFY pgrst, 'reload schema';


