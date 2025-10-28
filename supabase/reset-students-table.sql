-- students tablosunu tamamen resetle ve test verisi ekle

-- 1. RLS'i kapat
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 2. Tüm politikaları sil
DROP POLICY IF EXISTS "Allow service role access" ON students;
DROP POLICY IF EXISTS "Allow anonymous access" ON students;
DROP POLICY IF EXISTS "Allow public access" ON students;

-- 3. RLS'i AÇ
ALTER TABLE students ENABLE ROW LEVEL SECURITY;

-- 4. Service role için tam erişim
CREATE POLICY "Allow service role access" ON students
    FOR ALL TO service_role
    USING (true)
    WITH CHECK (true);

-- 5. Public için tam erişim
CREATE POLICY "Allow public access" ON students
    FOR ALL TO public
    USING (true)
    WITH CHECK (true);

-- 6. Test verileri ekle (eğer yoksa)
DO $$
DECLARE
    test_dept_id UUID;
    test_class_id UUID;
BEGIN
    -- Bilgisayar Mühendisliği
    SELECT id INTO test_dept_id FROM departments WHERE code = 'BM';
    IF test_dept_id IS NULL THEN
        INSERT INTO departments (name, code) VALUES ('Bilgisayar Mühendisliği', 'BM') RETURNING id INTO test_dept_id;
    END IF;
    
    -- 1-A sınıfı
    SELECT id INTO test_class_id FROM classes WHERE name = '1-A' AND department_id = test_dept_id;
    IF test_class_id IS NULL THEN
        INSERT INTO classes (name, department_id, academic_year) VALUES ('1-A', test_dept_id, '2024-2025') RETURNING id INTO test_class_id;
    END IF;
    
    -- Test öğrencileri
    INSERT INTO students (full_name, email, class_id, department_id, is_active) VALUES
    ('Ahmet Yılmaz', 'ahmet.yilmaz@student.edu', test_class_id, test_dept_id, true),
    ('Ayşe Demir', 'ayse.demir@student.edu', test_class_id, test_dept_id, true)
    ON CONFLICT (email) DO NOTHING;
END $$;

-- 7. Kontrol et
SELECT COUNT(*) as total_students FROM students;
SELECT id, full_name, email FROM students LIMIT 5;

