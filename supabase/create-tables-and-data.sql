-- Önce tabloları oluştur, sonra veri ekle

-- 1. Departments tablosu
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    code TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 2. Classes tablosu
CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    department_id UUID REFERENCES departments(id),
    academic_year TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 3. Students tablosu (eğer yoksa)
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    class_id UUID REFERENCES classes(id),
    department_id UUID REFERENCES departments(id),
    is_active BOOLEAN DEFAULT true,
    auth_user_id UUID REFERENCES auth.users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 4. RLS'i kapat (test için)
ALTER TABLE departments DISABLE ROW LEVEL SECURITY;
ALTER TABLE classes DISABLE ROW LEVEL SECURITY;
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 5. Test verilerini ekle
INSERT INTO departments (name, code) VALUES 
('Bilgisayar Mühendisliği', 'BM'),
('Elektrik-Elektronik Mühendisliği', 'EE'),
('Makine Mühendisliği', 'MM')
ON CONFLICT (code) DO NOTHING;

-- 6. Sınıfları ekle
INSERT INTO classes (name, department_id, academic_year) VALUES 
-- Bilgisayar Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Makine Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025')
ON CONFLICT DO NOTHING;

-- 7. Kontrol et
SELECT 'Departments:' as table_name, count(*) as count FROM departments
UNION ALL
SELECT 'Classes:', count(*) FROM classes;

-- 8. Verileri listele
SELECT id, name, code FROM departments ORDER BY name;
SELECT c.id, c.name, d.name as department_name FROM classes c JOIN departments d ON c.department_id = d.id ORDER BY d.name, c.name;
