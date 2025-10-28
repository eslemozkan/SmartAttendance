-- Sınıf ve şube sistemi için güncellenmiş veri

-- 1. Önce mevcut sınıfları sil ve yeniden oluştur
DELETE FROM classes;
DELETE FROM departments;

-- 2. Bölümleri ekle
INSERT INTO departments (name, code) VALUES 
('Bilgisayar Mühendisliği', 'BM'),
('Yazılım Mühendisliği', 'YM'),
('Elektrik-Elektronik Mühendisliği', 'EE'),
('Makine Mühendisliği', 'MM'),
('Endüstri Mühendisliği', 'EM'),
('İnşaat Mühendisliği', 'IM')
ON CONFLICT (code) DO NOTHING;

-- 3. Sınıfları ve şubeleri ekle (her sınıf için A, B, C şubeleri)
INSERT INTO classes (name, department_id, academic_year) VALUES 
-- Bilgisayar Mühendisliği - 1. Sınıf
('1-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('1-C', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Bilgisayar Mühendisliği - 2. Sınıf
('2-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-C', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Bilgisayar Mühendisliği - 3. Sınıf
('3-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('3-C', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Bilgisayar Mühendisliği - 4. Sınıf
('4-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('4-C', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Yazılım Mühendisliği - 1. Sınıf
('1-A', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('1-C', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),

-- Yazılım Mühendisliği - 2. Sınıf
('2-A', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('2-C', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),

-- Yazılım Mühendisliği - 3. Sınıf
('3-A', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('3-C', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),

-- Yazılım Mühendisliği - 4. Sınıf
('4-A', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),
('4-C', (SELECT id FROM departments WHERE code = 'YM'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği - 1. Sınıf
('1-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('1-C', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği - 2. Sınıf
('2-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-C', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği - 3. Sınıf
('3-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('3-C', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği - 4. Sınıf
('4-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('4-C', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Makine Mühendisliği - 1. Sınıf
('1-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('1-C', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),

-- Makine Mühendisliği - 2. Sınıf
('2-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-C', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),

-- Makine Mühendisliği - 3. Sınıf
('3-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('3-C', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),

-- Makine Mühendisliği - 4. Sınıf
('4-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('4-C', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025')
ON CONFLICT DO NOTHING;

-- 4. Kontrol et
SELECT 'Departments:' as table_name, count(*) as count FROM departments
UNION ALL
SELECT 'Classes:', count(*) FROM classes;

-- 5. Verileri listele
SELECT id, name, code FROM departments ORDER BY name;
SELECT c.id, c.name, d.name as department_name, d.code as department_code 
FROM classes c 
JOIN departments d ON c.department_id = d.id 
ORDER BY d.name, c.name;
