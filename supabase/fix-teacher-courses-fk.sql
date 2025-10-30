-- Align teacher_courses.course_id type with courses.id (bigint)

-- 1) Verify current types
SELECT 
  (SELECT data_type FROM information_schema.columns WHERE table_name='courses' AND column_name='id') AS courses_id_type,
  (SELECT data_type FROM information_schema.columns WHERE table_name='teacher_courses' AND column_name='course_id') AS tc_course_id_type
;

-- 2) Recreate teacher_courses with correct type if needed
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_name='teacher_courses' AND column_name='course_id' AND data_type <> 'bigint'
  ) THEN
    DROP TABLE IF EXISTS public.teacher_courses CASCADE;

    CREATE TABLE public.teacher_courses (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      teacher_id UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
      course_id BIGINT NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      UNIQUE(teacher_id, course_id)
    );

    ALTER TABLE public.teacher_courses DISABLE ROW LEVEL SECURITY;
    GRANT SELECT, INSERT, UPDATE, DELETE ON public.teacher_courses TO anon, authenticated;
    GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO anon, authenticated;
  END IF;
END $$;

NOTIFY pgrst, 'reload schema';


