-- Fix qr_codes.course_id to be BIGINT (to match courses.id)
-- This fixes the foreign key constraint error
-- Note: courses.id is BIGINT in the actual database (despite some schema files saying UUID)

-- Drop the foreign key constraint if it exists
ALTER TABLE public.qr_codes DROP CONSTRAINT IF EXISTS qr_codes_course_id_fkey;

-- Drop the column if it exists (we'll recreate it as BIGINT)
ALTER TABLE public.qr_codes DROP COLUMN IF EXISTS course_id;

-- Add course_id as BIGINT with foreign key to courses
ALTER TABLE public.qr_codes ADD COLUMN course_id BIGINT REFERENCES public.courses(id) ON DELETE CASCADE;

-- Recreate index
DROP INDEX IF EXISTS idx_qr_codes_course_week;
CREATE INDEX IF NOT EXISTS idx_qr_codes_course_week 
  ON public.qr_codes (course_id, week_number) 
  WHERE is_active = true;

-- Ensure RLS is disabled
ALTER TABLE public.qr_codes DISABLE ROW LEVEL SECURITY;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.qr_codes TO anon, authenticated;

NOTIFY pgrst, 'reload schema';

