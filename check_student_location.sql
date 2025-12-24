-- Öğrenci konum bilgilerini kontrol etmek için sorgular

-- 1. Son 10 yoklama kaydında öğrenci konum bilgilerini göster
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

-- 2. Son 24 saatte alınan yoklamalarda konum kontrolü (istatistik)
SELECT 
    COUNT(*) as toplam,
    COUNT(student_latitude) as konumlu,
    COUNT(*) - COUNT(student_latitude) as konumsuz
FROM public.attendances
WHERE marked_at >= NOW() - INTERVAL '24 hours';

