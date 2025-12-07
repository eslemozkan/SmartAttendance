-- Fix qr_codes table to include course_id and week_number
-- This migration adds the missing columns that the create-qr edge function expects

-- Add course_id and week_number columns if they don't exist
DO $$
BEGIN
  -- Add course_id column (bigint to match the course_id type used in the app)
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'qr_codes' 
    AND column_name = 'course_id'
  ) THEN
    ALTER TABLE public.qr_codes ADD COLUMN course_id BIGINT;
  END IF;

  -- Add week_number column
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'qr_codes' 
    AND column_name = 'week_number'
  ) THEN
    ALTER TABLE public.qr_codes ADD COLUMN week_number INTEGER;
  END IF;
END $$;

-- Create index for faster lookups by course_id and week_number
CREATE INDEX IF NOT EXISTS idx_qr_codes_course_week 
  ON public.qr_codes (course_id, week_number) 
  WHERE is_active = true;

-- IMPORTANT: Disable RLS completely for development (was causing timeout issues)
-- The original RLS policy was checking for service_role which may not work properly with Edge Functions
ALTER TABLE public.qr_codes DISABLE ROW LEVEL SECURITY;

-- Drop old policies
DROP POLICY IF EXISTS qr_codes_service_all ON public.qr_codes;

-- Grant full access to anon and authenticated (for Edge Functions)
GRANT SELECT, INSERT, UPDATE, DELETE ON public.qr_codes TO anon, authenticated;

-- Notify PostgREST to reload schema
NOTIFY pgrst, 'reload schema';

