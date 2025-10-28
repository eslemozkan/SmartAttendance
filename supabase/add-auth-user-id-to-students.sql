-- students tablosuna auth_user_id sütunu ekle
ALTER TABLE students ADD COLUMN IF NOT EXISTS auth_user_id UUID REFERENCES auth.users(id);

-- Index ekle (performans için)
CREATE INDEX IF NOT EXISTS idx_students_auth_user_id ON students(auth_user_id);

-- RLS policy güncelle (auth_user_id ile erişim)
DROP POLICY IF EXISTS "Students can view own data" ON students;
CREATE POLICY "Students can view own data" ON students
    FOR SELECT USING (auth_user_id = auth.uid());

-- Admin'ler tüm öğrencileri görebilir
DROP POLICY IF EXISTS "Admins can manage students" ON students;
CREATE POLICY "Admins can manage students" ON students
    FOR ALL USING (
        EXISTS (
            SELECT 1 FROM profiles 
            WHERE id = auth.uid() AND role IN ('admin', 'super_admin')
        )
    );
