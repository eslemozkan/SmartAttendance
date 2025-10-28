-- students tablosunda veri var mı kontrol et

-- 1. students tablosunda kaç öğrenci var?
SELECT COUNT(*) as total_students FROM students;

-- 2. students tablosundaki tüm öğrencileri listele
SELECT 
    id, 
    full_name, 
    email, 
    class_id, 
    department_id, 
    auth_user_id,
    is_active,
    created_at
FROM students 
ORDER BY created_at DESC;

-- 3. Herhangi bir öğrenci var mı?
SELECT 
    CASE 
        WHEN COUNT(*) > 0 THEN 'Evet, ' || COUNT(*) || ' öğrenci var'
        ELSE 'Hayır, tablo boş!'
    END as result
FROM students;

