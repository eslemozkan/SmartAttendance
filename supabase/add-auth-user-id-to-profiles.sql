-- Add auth_user_id to profiles for linking to Supabase Auth users

ALTER TABLE public.profiles
ADD COLUMN IF NOT EXISTS auth_user_id UUID;

CREATE INDEX IF NOT EXISTS idx_profiles_auth_user_id ON public.profiles(auth_user_id);

-- Optional uniqueness if you want 1:1 mapping (comment out if not desired)
-- CREATE UNIQUE INDEX IF NOT EXISTS ux_profiles_auth_user_id ON public.profiles(auth_user_id) WHERE auth_user_id IS NOT NULL;

-- Verify
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'profiles' AND column_name = 'auth_user_id';

NOTIFY pgrst, 'reload schema';


