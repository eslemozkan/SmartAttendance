-- profiles tablosuna department_id sütunu ekle (eğer yoksa)

ALTER TABLE profiles ADD COLUMN IF NOT EXISTS department_id UUID REFERENCES departments(id);

-- Index ekle
CREATE INDEX IF NOT EXISTS idx_profiles_department_id ON profiles(department_id);

-- Kontrol et
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'profiles' AND column_name = 'department_id';

