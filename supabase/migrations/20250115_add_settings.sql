-- Settings table for system configuration
CREATE TABLE IF NOT EXISTS public.settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key TEXT UNIQUE NOT NULL,
    value TEXT NOT NULL,
    description TEXT,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_settings_key ON public.settings(key);

-- Disable RLS for admin access (dev-friendly)
ALTER TABLE public.settings DISABLE ROW LEVEL SECURITY;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.settings TO anon, authenticated;

-- Insert default settings
INSERT INTO public.settings (key, value, description) VALUES
    ('current_academic_year', '', 'Mevcut akademik yıl (örn: 2024-2025)'),
    ('current_semester', 'Güz', 'Mevcut dönem (Güz, Bahar, Yaz)')
ON CONFLICT (key) DO NOTHING;







