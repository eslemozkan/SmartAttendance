-- Hızlı test için örnek veriler
-- Bu dosyayı Supabase SQL Editor'da çalıştır

-- Bölümler
INSERT INTO departments (name, code) VALUES 
('Bilgisayar Mühendisliği', 'BM'),
('Elektrik-Elektronik Mühendisliği', 'EE'),
('Makine Mühendisliği', 'MM')
ON CONFLICT (code) DO NOTHING;

-- Sınıflar (Bilgisayar Mühendisliği)
INSERT INTO classes (name, department_id, academic_year) VALUES 
('1-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025')
ON CONFLICT DO NOTHING;

-- Sınıflar (Elektrik-Elektronik)
INSERT INTO classes (name, department_id, academic_year) VALUES 
('1-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025')
ON CONFLICT DO NOTHING;

-- Sınıflar (Makine Mühendisliği)
INSERT INTO classes (name, department_id, academic_year) VALUES 
('1-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025')
ON CONFLICT DO NOTHING;

-- Kontrol sorguları
SELECT 'Departments:' as table_name, count(*) as count FROM departments
UNION ALL
SELECT 'Classes:', count(*) FROM classes
UNION ALL
SELECT 'Students:', count(*) FROM students;

-- Tüm bölümleri listele
SELECT id, name, code FROM departments ORDER BY name;

-- Tüm sınıfları listele (bölüm adı ile)
SELECT c.id, c.name, d.name as department_name, c.academic_year 
FROM classes c 
JOIN departments d ON c.department_id = d.id 
ORDER BY d.name, c.name;
