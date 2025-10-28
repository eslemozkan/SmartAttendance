-- students tablosu için RLS düzeltmesi

-- 1. Önce RLS'i kapat (tüm politikaları temizlemek için)
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 2. Tüm mevcut politikaları sil
DROP POLICY IF EXISTS "Students can view own data" ON students;
DROP POLICY IF EXISTS "Admins can manage students" ON students;
DROP POLICY IF EXISTS "Allow service role access" ON students;
DROP POLICY IF EXISTS "Allow anonymous access" ON students;

-- 3. RLS'i tekrar aç
ALTER TABLE students ENABLE ROW LEVEL SECURITY;

-- 4. Service role için tam erişim (Edge Function için)
CREATE POLICY "Allow service role access" ON students
    FOR ALL USING (auth.role() = 'service_role');

-- 5. Anonymous erişim için SELECT izni
CREATE POLICY "Allow anonymous access" ON students
    FOR SELECT USING (true);

-- 6. Kontrol et
SELECT 
    schemaname, 
    tablename, 
    policyname, 
    permissive, 
    roles, 
    cmd, 
    qual 
FROM pg_policies 
WHERE tablename = 'students';

