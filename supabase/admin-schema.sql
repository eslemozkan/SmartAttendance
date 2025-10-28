-- Admin Paneli için Genişletilmiş Veritabanı Şeması

-- 1. Departments (Bölümler) Tablosu
CREATE TABLE IF NOT EXISTS departments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    code TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 2. Classes (Sınıflar) Tablosu
CREATE TABLE IF NOT EXISTS classes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL, -- "1-A", "2-B" gibi
    department_id UUID REFERENCES departments(id),
    academic_year TEXT NOT NULL, -- "2024-2025"
    created_at TIMESTAMP DEFAULT NOW()
);

-- 3. Admins Tablosu
CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'admin', -- 'super_admin', 'admin'
    department_id UUID REFERENCES departments(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 4. Teachers Tablosu (Güncellenmiş)
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS department_id UUID REFERENCES departments(id);
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS employee_id TEXT UNIQUE;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS phone TEXT;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS is_active BOOLEAN DEFAULT true;

-- 5. Students Tablosu (Yeni)
CREATE TABLE IF NOT EXISTS students (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_number TEXT UNIQUE NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    phone TEXT,
    class_id UUID REFERENCES classes(id),
    department_id UUID REFERENCES departments(id),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 6. Course-Class Assignments (Ders-Sınıf Atamaları)
CREATE TABLE IF NOT EXISTS course_class_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    course_id UUID REFERENCES courses(id),
    class_id UUID REFERENCES classes(id),
    teacher_id UUID REFERENCES profiles(id),
    academic_year TEXT NOT NULL,
    semester TEXT NOT NULL, -- "Güz", "Bahar"
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(course_id, class_id, academic_year, semester)
);

-- 7. Student Enrollments (Öğrenci Kayıtları) - Güncellenmiş
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS academic_year TEXT;
ALTER TABLE enrollments ADD COLUMN IF NOT EXISTS semester TEXT;

-- 8. Attendance Records (Yoklama Kayıtları) - Güncellenmiş
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS student_number TEXT;
ALTER TABLE attendances ADD COLUMN IF NOT EXISTS class_id UUID REFERENCES classes(id);

-- 9. RLS Policies (Row Level Security)

-- Admins için policies
ALTER TABLE admins ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can view all admins" ON admins
    FOR SELECT USING (true);

CREATE POLICY "Admins can insert admins" ON admins
    FOR INSERT WITH CHECK (true);

-- Students için policies
ALTER TABLE students ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can manage students" ON students
    FOR ALL USING (true);

-- Classes için policies
ALTER TABLE classes ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can manage classes" ON classes
    FOR ALL USING (true);

-- Departments için policies
ALTER TABLE departments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can manage departments" ON departments
    FOR ALL USING (true);

-- Course-Class Assignments için policies
ALTER TABLE course_class_assignments ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can manage course assignments" ON course_class_assignments
    FOR ALL USING (true);

-- 10. Indexes (Performans için)
CREATE INDEX IF NOT EXISTS idx_students_class_id ON students(class_id);
CREATE INDEX IF NOT EXISTS idx_students_department_id ON students(department_id);
CREATE INDEX IF NOT EXISTS idx_course_class_assignments_course_id ON course_class_assignments(course_id);
CREATE INDEX IF NOT EXISTS idx_course_class_assignments_class_id ON course_class_assignments(class_id);
CREATE INDEX IF NOT EXISTS idx_attendances_class_id ON attendances(class_id);

-- 11. Örnek Veriler
INSERT INTO departments (name, code) VALUES 
('Bilgisayar Mühendisliği', 'BM'),
('Elektrik-Elektronik Mühendisliği', 'EE'),
('Makine Mühendisliği', 'MM'),
('Endüstri Mühendisliği', 'EM'),
('İnşaat Mühendisliği', 'IM')
ON CONFLICT (code) DO NOTHING;

INSERT INTO classes (name, department_id, academic_year) VALUES 
-- Bilgisayar Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('3-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('4-A', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'BM'), '2024-2025'),

-- Elektrik-Elektronik Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('3-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('4-A', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'EE'), '2024-2025'),

-- Makine Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('3-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('4-A', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'MM'), '2024-2025'),

-- Endüstri Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('3-A', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('4-A', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'EM'), '2024-2025'),

-- İnşaat Mühendisliği
('1-A', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('1-B', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('2-A', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('2-B', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('3-A', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('3-B', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('4-A', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025'),
('4-B', (SELECT id FROM departments WHERE code = 'IM'), '2024-2025')
ON CONFLICT DO NOTHING;

-- Admin kullanıcısı (şifre: admin123)
INSERT INTO admins (email, password_hash, full_name, role) VALUES 
('admin@university.edu', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Sistem Yöneticisi', 'super_admin')
ON CONFLICT (email) DO NOTHING;


