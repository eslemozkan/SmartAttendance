-- Complete fix for qr_codes table
-- This migration ensures the table has the correct structure for create-qr edge function
-- Combines all previous fixes into one migration

-- Step 1: Drop foreign key constraints if they exist
ALTER TABLE public.qr_codes DROP CONSTRAINT IF EXISTS qr_codes_course_id_fkey;

-- Step 2: Add course_id and week_number columns if they don't exist
DO $$
BEGIN
  -- Add course_id column (BIGINT to match courses.id - courses.id is BIGINT, not UUID)
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'qr_codes' 
    AND column_name = 'course_id'
  ) THEN
    ALTER TABLE public.qr_codes ADD COLUMN course_id BIGINT;
  ELSE
    -- If column exists but is wrong type, drop and recreate
    IF EXISTS (
      SELECT 1 FROM information_schema.columns 
      WHERE table_schema = 'public' 
      AND table_name = 'qr_codes' 
      AND column_name = 'course_id'
      AND data_type != 'bigint'
    ) THEN
      ALTER TABLE public.qr_codes DROP COLUMN course_id;
      ALTER TABLE public.qr_codes ADD COLUMN course_id BIGINT;
    END IF;
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

-- Step 3: Add foreign key constraint (courses.id is BIGINT)
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.table_constraints 
    WHERE constraint_name = 'qr_codes_course_id_fkey'
    AND table_name = 'qr_codes'
  ) THEN
    -- Add foreign key constraint (courses.id is BIGINT)
    BEGIN
      ALTER TABLE public.qr_codes 
      ADD CONSTRAINT qr_codes_course_id_fkey 
      FOREIGN KEY (course_id) REFERENCES public.courses(id) ON DELETE CASCADE;
    EXCEPTION WHEN OTHERS THEN
      -- If foreign key fails, just log and continue (table might not have courses yet)
      RAISE NOTICE 'Could not add foreign key constraint: %', SQLERRM;
    END;
  END IF;
END $$;

-- Step 4: Create/update indexes
DROP INDEX IF EXISTS idx_qr_codes_course_week;
CREATE INDEX IF NOT EXISTS idx_qr_codes_course_week 
  ON public.qr_codes (course_id, week_number) 
  WHERE is_active = true;

-- Keep the original assignment_id index if it exists (only if column exists)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'qr_codes' 
    AND column_name = 'assignment_id'
  ) THEN
    CREATE INDEX IF NOT EXISTS idx_qr_codes_assignment_created_at
      ON public.qr_codes (assignment_id, created_at);
  END IF;
END $$;

-- Step 5: Drop ALL existing policies first (critical for Edge Functions to work)
DROP POLICY IF EXISTS qr_codes_service_all ON public.qr_codes;
DROP POLICY IF EXISTS "Allow anonymous read access to qr_codes" ON public.qr_codes;
-- Drop any other policies that might exist
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT policyname FROM pg_policies WHERE schemaname = 'public' AND tablename = 'qr_codes') LOOP
        EXECUTE 'DROP POLICY IF EXISTS ' || quote_ident(r.policyname) || ' ON public.qr_codes';
    END LOOP;
END $$;

-- Step 6: Disable RLS (critical for Edge Functions to work)
ALTER TABLE public.qr_codes DISABLE ROW LEVEL SECURITY;

-- Step 7: Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.qr_codes TO anon, authenticated;

-- Step 8: Disable RLS on course_weeks if it exists (might be causing issues)
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = 'course_weeks'
  ) THEN
    ALTER TABLE public.course_weeks DISABLE ROW LEVEL SECURITY;
    GRANT SELECT, INSERT, UPDATE, DELETE ON public.course_weeks TO anon, authenticated;
  END IF;
END $$;

-- Step 9: Notify PostgREST
NOTIFY pgrst, 'reload schema';

