-- Add weekly_hours column to courses table
-- This represents how many hours per week a course is taught

-- Add weekly_hours column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'courses' 
        AND column_name = 'weekly_hours'
    ) THEN
        ALTER TABLE public.courses 
        ADD COLUMN weekly_hours INTEGER NOT NULL DEFAULT 2 
        CHECK (weekly_hours > 0 AND weekly_hours <= 10);
        
        COMMENT ON COLUMN public.courses.weekly_hours IS 'Haftalık ders saati sayısı (1-10 arası)';
    END IF;
END $$;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_courses_weekly_hours 
ON public.courses(weekly_hours);

NOTIFY pgrst, 'reload schema';



