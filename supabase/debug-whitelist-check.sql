-- students tablosunu kontrol et ve RLS politikalarını düzelt

-- 1. students tablosunda kaç veri var?
SELECT COUNT(*) as total_students FROM students;

-- 2. Öğrencileri listele
SELECT id, full_name, email, class_id, department_id FROM students;

-- 3. RLS durumunu kontrol et
SELECT schemaname, tablename, rowsecurity 
FROM pg_tables 
WHERE tablename = 'students';

-- 4. RLS'i KAPAT (tamamen kapalı tut)
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 5. Tüm politikaları sil
DROP POLICY IF EXISTS "Allow service role access" ON students;
DROP POLICY IF EXISTS "Allow anonymous access" ON students;
DROP POLICY IF EXISTS "Allow public access" ON students;
DROP POLICY IF EXISTS "Students can view own data" ON students;
DROP POLICY IF EXISTS "Admins can manage students" ON students;

-- 6. Grant permissions
GRANT ALL ON students TO anon;
GRANT ALL ON students TO authenticated;
GRANT ALL ON students TO service_role;

-- 7. Kontrol et
SELECT tablename, tablename || ' - RLS: ' || rowsecurity as info
FROM pg_tables 
WHERE tablename = 'students';

