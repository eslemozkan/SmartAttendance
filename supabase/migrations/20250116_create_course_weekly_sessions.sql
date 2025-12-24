-- Create course_weekly_sessions table
-- This table tracks which course sessions have been completed for each week
-- Example: A course with 4 weekly hours will have 4 sessions per week (session_number: 1, 2, 3, 4)

CREATE TABLE IF NOT EXISTS public.course_weekly_sessions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
    week_number INTEGER NOT NULL CHECK (week_number > 0 AND week_number <= 20),
    session_number INTEGER NOT NULL CHECK (session_number > 0),
    qr_code_id UUID REFERENCES public.qr_codes(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(course_id, week_number, session_number)
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_course_weekly_sessions_course_week 
ON public.course_weekly_sessions(course_id, week_number);

CREATE INDEX IF NOT EXISTS idx_course_weekly_sessions_qr_code 
ON public.course_weekly_sessions(qr_code_id) 
WHERE qr_code_id IS NOT NULL;

-- Disable RLS for easier access (adjust as needed for production)
ALTER TABLE public.course_weekly_sessions DISABLE ROW LEVEL SECURITY;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.course_weekly_sessions TO anon, authenticated;
GRANT USAGE, SELECT ON SEQUENCE public.course_weekly_sessions_id_seq TO anon, authenticated;

COMMENT ON TABLE public.course_weekly_sessions IS 'Haftalık ders oturumlarını takip eder. Her ders için haftalık saat sayısı kadar oturum oluşturulur.';
COMMENT ON COLUMN public.course_weekly_sessions.session_number IS 'Hafta içindeki oturum numarası (1, 2, 3, 4... weekly_hours kadar)';
COMMENT ON COLUMN public.course_weekly_sessions.qr_code_id IS 'Bu oturum için oluşturulan QR kod ID (null ise henüz işlenmemiş)';

NOTIFY pgrst, 'reload schema';



