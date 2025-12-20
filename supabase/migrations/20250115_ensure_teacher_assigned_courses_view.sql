-- Ensure teacher_assigned_courses view exists and is accessible
-- This view is critical for the mobile app to load teacher courses

-- Drop and recreate view to ensure it's up to date
DROP VIEW IF EXISTS public.teacher_assigned_courses CASCADE;

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

-- Grant permissions
GRANT SELECT ON public.teacher_assigned_courses TO anon, authenticated;

-- Ensure RLS is disabled on underlying tables (for development)
ALTER TABLE public.teacher_courses DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.courses DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.profiles DISABLE ROW LEVEL SECURITY;

-- Grant permissions on underlying tables
GRANT SELECT ON public.teacher_courses TO anon, authenticated;
GRANT SELECT ON public.courses TO anon, authenticated;
GRANT SELECT ON public.profiles TO anon, authenticated;

-- Notify PostgREST to reload schema
NOTIFY pgrst, 'reload schema';








