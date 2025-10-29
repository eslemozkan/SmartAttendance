-- profiles tablosunu yenile ve cache'i temizle

-- Önce sütunu kontrol et
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'profiles' AND column_name = 'department_id';

-- Eğer sütun YOKSA, ekle
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS department_id UUID REFERENCES departments(id);

-- Index ekle
CREATE INDEX IF NOT EXISTS idx_profiles_department_id ON profiles(department_id);

-- Cache'i temizlemek için basit bir query
SELECT COUNT(*) as total_profiles FROM profiles;

