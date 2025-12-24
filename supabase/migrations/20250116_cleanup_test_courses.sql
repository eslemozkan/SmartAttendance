-- Temizlik: Test amaçlı eklenmiş dersleri temizle
-- Bu script, anlamsız isim/kod içeren test derslerini siler

-- Önce hangi derslerin silineceğini göster (kontrol için)
SELECT 
    id,
    name,
    code,
    department_id,
    created_at
FROM public.courses
WHERE 
    -- Tek harfli veya çok kısa isimler
    LENGTH(TRIM(name)) <= 2
    OR
    -- Anlamsız kodlar (tek harf, çok kısa)
    (code IS NOT NULL AND LENGTH(TRIM(code)) <= 2)
    OR
    -- Test amaçlı görünen isimler
    LOWER(name) IN ('a', 'e', 'r', 's', 't', 'gg', 'qq', 'ai', 'l')
    OR
    -- Tekrarlayan karakterler içeren isimler (test verisi gibi görünen)
    name ~ '^(.)\1+$'
    OR
    -- Anlamsız kombinasyonlar
    name ~ '^[a-z]{1,3}$'
ORDER BY created_at DESC;

-- Eğer yukarıdaki liste doğruysa, aşağıdaki DELETE'i çalıştır
-- DİKKAT: Bu işlem geri alınamaz! Önce yukarıdaki SELECT ile kontrol edin.

-- Silme işlemi (yorum satırını kaldırın)
/*
DELETE FROM public.courses
WHERE 
    LENGTH(TRIM(name)) <= 2
    OR
    (code IS NOT NULL AND LENGTH(TRIM(code)) <= 2)
    OR
    LOWER(name) IN ('a', 'e', 'r', 's', 't', 'gg', 'qq', 'ai', 'l')
    OR
    name ~ '^(.)\1+$'
    OR
    name ~ '^[a-z]{1,3}$';

-- İlişkili kayıtları da temizle (cascade olmalı ama emin olmak için)
DELETE FROM public.teacher_courses
WHERE course_id NOT IN (SELECT id FROM public.courses);

DELETE FROM public.course_class_assignments
WHERE course_id NOT IN (SELECT id FROM public.courses);

DELETE FROM public.qr_codes
WHERE course_id NOT IN (SELECT id FROM public.courses);

DELETE FROM public.attendances
WHERE course_id NOT IN (SELECT id FROM public.courses);

NOTIFY pgrst, 'reload schema';
*/



