-- Add session_number column to attendances table
-- This allows tracking attendance for each session separately

-- Add session_number column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'attendances' 
        AND column_name = 'session_number'
    ) THEN
        ALTER TABLE public.attendances 
        ADD COLUMN session_number INTEGER;
        
        COMMENT ON COLUMN public.attendances.session_number IS 'Haftalık ders oturum numarası (1, 2, 3, 4...). NULL ise tek oturum varsayılır (geriye dönük uyumluluk)';
    END IF;
END $$;

-- Drop old unique constraint if it exists
DROP INDEX IF EXISTS uq_attendance_assignment_student_day;

-- Create new unique constraint that includes session_number
-- This prevents duplicate attendance for the same course/week/session/student
-- Note: We use a partial index approach for NULL session_number (backward compatibility)
-- Date check is handled at application level, not in database constraint

-- First, create unique index for non-NULL session_number
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_course_week_session_student
ON public.attendances (
  course_id,
  week_number,
  session_number,
  student_id
)
WHERE session_number IS NOT NULL;

-- Create separate unique index for NULL session_number (backward compatibility)
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_course_week_null_session_student
ON public.attendances (
  course_id,
  week_number,
  student_id
)
WHERE session_number IS NULL;

-- Create index for faster queries by session_number
CREATE INDEX IF NOT EXISTS idx_attendances_session_number 
ON public.attendances(session_number) 
WHERE session_number IS NOT NULL;

COMMENT ON TABLE public.attendances IS 'Yoklama kayıtları. Her oturum için ayrı kayıt oluşturulur.';

NOTIFY pgrst, 'reload schema';

