-- Add teacher location columns to qr_codes table
ALTER TABLE public.qr_codes 
ADD COLUMN IF NOT EXISTS teacher_latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS teacher_longitude DOUBLE PRECISION;

-- Add student location columns to attendances table
ALTER TABLE public.attendances 
ADD COLUMN IF NOT EXISTS student_latitude DOUBLE PRECISION,
ADD COLUMN IF NOT EXISTS student_longitude DOUBLE PRECISION;

-- Add index for location-based queries
CREATE INDEX IF NOT EXISTS idx_qr_codes_location 
ON public.qr_codes (teacher_latitude, teacher_longitude) 
WHERE teacher_latitude IS NOT NULL AND teacher_longitude IS NOT NULL;

