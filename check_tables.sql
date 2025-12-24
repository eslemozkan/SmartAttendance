-- Tabloları kontrol etmek için SQL sorguları
-- Bu sorguları Supabase SQL Editor'de çalıştırın ve sonuçları paylaşın

-- 1. courses tablosunun yapısını kontrol et
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'courses'
ORDER BY ordinal_position;

-- 2. courses tablosundaki örnek verileri göster (ilk 5 kayıt)
SELECT 
    id,
    name,
    code,
    department_id,
    created_at
FROM public.courses
LIMIT 5;

-- 3. qr_codes tablosunun yapısını kontrol et
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_schema = 'public' 
  AND table_name = 'qr_codes'
ORDER BY ordinal_position;

-- 4. qr_codes tablosundaki foreign key constraint'leri kontrol et
SELECT
    tc.constraint_name,
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name = 'qr_codes';

-- 5. teacher_courses tablosundaki örnek verileri göster (ilk 5 kayıt)
SELECT 
    id,
    teacher_id,
    course_id,
    created_at
FROM public.teacher_courses
LIMIT 5;











