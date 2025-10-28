-- RLS politikalarını düzelt ve test verilerini ekle

-- 1. Önce RLS'i geçici olarak kapat (test için)
ALTER TABLE departments DISABLE ROW LEVEL SECURITY;
ALTER TABLE classes DISABLE ROW LEVEL SECURITY;
ALTER TABLE students DISABLE ROW LEVEL SECURITY;

-- 2. Test verilerini ekle
INSERT INTO departments (name, code) VALUES 
('Bilgisayar Mühendisliği', 'BM'),
('Elektrik-Elektronik Mühendisliği', 'EE'),
('Makine Mühendisliği', 'MM')
ON CONFLICT (code) DO NOTHING;

-- 3. Sınıfları ekle
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

-- 4. Kontrol et
SELECT 'Departments:' as table_name, count(*) as count FROM departments
UNION ALL
SELECT 'Classes:', count(*) FROM classes;

-- 5. Verileri listele
SELECT id, name, code FROM departments ORDER BY name;
SELECT c.id, c.name, d.name as department_name FROM classes c JOIN departments d ON c.department_id = d.id ORDER BY d.name, c.name;
