-- Konum verilerinin veritabanına kaydedilip kaydedilmediğini kontrol etmek için SQL sorguları

-- 1. QR kodlarında öğretmen konumu kontrolü
-- Son 10 QR kodun öğretmen konum bilgilerini göster
SELECT 
    id,
    course_id,
    week_number,
    created_at,
    teacher_latitude,
    teacher_longitude,
    CASE 
        WHEN teacher_latitude IS NOT NULL AND teacher_longitude IS NOT NULL THEN 'VAR'
        ELSE 'YOK'
    END AS konum_durumu
FROM public.qr_codes
ORDER BY created_at DESC
LIMIT 10;

-- 2. Yoklama kayıtlarında öğrenci konumu kontrolü
-- Son 10 yoklama kaydının öğrenci konum bilgilerini göster
SELECT 
    id,
    course_id,
    week_number,
    session_number,
    student_id,
    marked_at,
    student_latitude,
    student_longitude,
    CASE 
        WHEN student_latitude IS NOT NULL AND student_longitude IS NOT NULL THEN 'VAR'
        ELSE 'YOK'
    END AS konum_durumu
FROM public.attendances
ORDER BY marked_at DESC
LIMIT 10;

-- 3. İstatistikler: Kaç QR kodunda öğretmen konumu var?
SELECT 
    COUNT(*) as toplam_qr,
    COUNT(teacher_latitude) as konumlu_qr,
    COUNT(*) - COUNT(teacher_latitude) as konumsuz_qr,
    ROUND(COUNT(teacher_latitude) * 100.0 / COUNT(*), 2) as konum_yuzdesi
FROM public.qr_codes
WHERE created_at >= NOW() - INTERVAL '30 days';

-- 4. İstatistikler: Kaç yoklama kaydında öğrenci konumu var?
SELECT 
    COUNT(*) as toplam_yoklama,
    COUNT(student_latitude) as konumlu_yoklama,
    COUNT(*) - COUNT(student_latitude) as konumsuz_yoklama,
    ROUND(COUNT(student_latitude) * 100.0 / COUNT(*), 2) as konum_yuzdesi
FROM public.attendances
WHERE marked_at >= NOW() - INTERVAL '30 days';

-- 5. Son 24 saatte oluşturulan QR kodlarında konum kontrolü
SELECT 
    COUNT(*) as toplam,
    COUNT(teacher_latitude) as konumlu,
    COUNT(*) - COUNT(teacher_latitude) as konumsuz
FROM public.qr_codes
WHERE created_at >= NOW() - INTERVAL '24 hours';

-- 6. Son 24 saatte alınan yoklamalarda konum kontrolü
SELECT 
    COUNT(*) as toplam,
    COUNT(student_latitude) as konumlu,
    COUNT(*) - COUNT(student_latitude) as konumsuz
FROM public.attendances
WHERE marked_at >= NOW() - INTERVAL '24 hours';

