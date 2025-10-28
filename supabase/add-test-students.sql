-- Test öğrencileri ekle

-- Önce bir bölüm ve sınıf seç
DO $$
DECLARE
    test_dept_id UUID;
    test_class_id UUID;
BEGIN
    -- Bilgisayar Mühendisliği bölümünü al veya oluştur
    SELECT id INTO test_dept_id FROM departments WHERE code = 'BM';
    IF test_dept_id IS NULL THEN
        INSERT INTO departments (name, code) VALUES ('Bilgisayar Mühendisliği', 'BM') RETURNING id INTO test_dept_id;
    END IF;
    
    -- 1-A sınıfını al veya oluştur
    SELECT id INTO test_class_id FROM classes WHERE name = '1-A' AND department_id = test_dept_id;
    IF test_class_id IS NULL THEN
        INSERT INTO classes (name, department_id, academic_year) VALUES ('1-A', test_dept_id, '2024-2025') RETURNING id INTO test_class_id;
    END IF;
    
    -- Test öğrencileri ekle
    INSERT INTO students (full_name, email, class_id, department_id, is_active) VALUES
    ('Ahmet Yılmaz', 'ahmet.yilmaz@student.edu', test_class_id, test_dept_id, true),
    ('Ayşe Demir', 'ayse.demir@student.edu', test_class_id, test_dept_id, true),
    ('Mehmet Kaya', 'mehmet.kaya@student.edu', test_class_id, test_dept_id, true)
    ON CONFLICT (email) DO NOTHING;
    
    RAISE NOTICE 'Test öğrencileri eklendi!';
END $$;

-- Kontrol et
SELECT 
    s.id, 
    s.full_name, 
    s.email, 
    s.is_active,
    d.name as department_name,
    c.name as class_name
FROM students s
LEFT JOIN departments d ON s.department_id = d.id
LEFT JOIN classes c ON s.class_id = c.id;

