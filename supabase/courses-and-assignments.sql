-- Create courses and teacher_courses (dev-friendly permissions)

CREATE TABLE IF NOT EXISTS public.courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  code TEXT,
  department_id UUID REFERENCES public.departments(id) ON DELETE SET NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_courses_department_id ON public.courses(department_id);

CREATE TABLE IF NOT EXISTS public.teacher_courses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  teacher_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
  course_id UUID NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE(teacher_id, course_id)
);

-- Dev: disable RLS and grant open access for demo
ALTER TABLE public.courses DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.teacher_courses DISABLE ROW LEVEL SECURITY;

GRANT SELECT, INSERT, UPDATE, DELETE ON public.courses TO anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.teacher_courses TO anon, authenticated;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;

NOTIFY pgrst, 'reload schema';



