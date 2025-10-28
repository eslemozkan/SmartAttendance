-- students tablosu için RLS politikaları

-- 1. RLS'i kapat (tüm politikaları temizlemek için)
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 2. Tüm mevcut politikaları sil
DROP POLICY IF EXISTS "Allow service role access" ON students;
DROP POLICY IF EXISTS "Allow anonymous access" ON students;
DROP POLICY IF EXISTS "Allow public access" ON students;

-- 3. students tablosunda public (anon) erişimine izin ver
GRANT SELECT, INSERT, UPDATE ON students TO anon;
GRANT SELECT, INSERT, UPDATE ON students TO authenticated;

-- 4. RLS'i aç
ALTER TABLE students ENABLE ROW LEVEL SECURITY;

-- 5. Service role için tam erişim
CREATE POLICY "Allow service role access" ON students
    FOR ALL TO service_role
    USING (true)
    WITH CHECK (true);

-- 6. Public (anon) için SELECT ve INSERT izni
CREATE POLICY "Allow public access" ON students
    FOR ALL TO public
    USING (true)
    WITH CHECK (true);

-- 7. Kontrol et
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

