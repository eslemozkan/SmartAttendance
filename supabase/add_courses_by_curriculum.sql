-- Müfredata göre dersleri ekle ve dinamik yapı oluştur
-- Bu script mevcut müfredata göre dersleri ekler ve sınıf seviyelerini otomatik belirler

-- 1. Önce mevcut dersleri kontrol et
SELECT 
    'Mevcut Dersler' AS info,
    COUNT(*) AS count
FROM public.courses;

-- 2. Bölümleri kontrol et
SELECT id, name, code 
FROM public.departments 
ORDER BY code;

-- 3. Müfredata göre dersleri ekle (mevcut değilse)
-- Bilgisayar Mühendisliği (CENG) dersleri
INSERT INTO public.courses (name, code, department_id)
SELECT 
    course_data.name,
    course_data.code,
    d.id
FROM (VALUES
    -- 1. Sınıf
    ('Programlamaya Giriş', 'CENG101', 'CENG'),
    ('Sayısal Mantık', 'CENG103', 'CENG'),
    ('Veri Yapıları', 'CENG201', 'CENG'),
    ('Nesne Yönelimli Programlama', 'CENG202', 'CENG'),
    
    -- 2. Sınıf
    ('Algoritma Analizi', 'CENG301', 'CENG'),
    ('Veritabanı Sistemleri', 'CENG302', 'CENG'),
    ('Bilgisayar Ağları', 'CENG303', 'CENG'),
    ('Yazılım Mühendisliği', 'CENG304', 'CENG'),
    
    -- 3. Sınıf
    ('İşletim Sistemleri', 'CENG401', 'CENG'),
    ('Ağ Teknolojileri', 'CENG402', 'CENG'),
    ('Web Programlama', 'CENG403', 'CENG'),
    ('Mobil Programlama', 'CENG404', 'CENG'),
    
    -- 4. Sınıf
    ('Bitirme Projesi', 'CENG501', 'CENG'),
    ('Staj', 'CENG502', 'CENG'),
    
    -- Yazılım Mühendisliği (YAZM) dersleri
    -- 1. Sınıf
    ('Algoritmalar', 'YAZM101', 'YAZ'),
    ('Programlama Temelleri', 'YAZM102', 'YAZ'),
    ('Veri Yapıları ve Algoritmalar', 'YAZM103', 'YAZ'),
    
    -- 2. Sınıf
    ('Veritabanı Sistemleri', 'YAZM201', 'YAZ'),
    ('Web Programlama', 'YAZM202', 'YAZ'),
    ('Yazılım Tasarımı', 'YAZM203', 'YAZ'),
    
    -- 3. Sınıf
    ('Yazılım Testi', 'YAZM301', 'YAZ'),
    ('Proje Yönetimi', 'YAZM302', 'YAZ'),
    ('Mikroservis Mimarisi', 'YAZM303', 'YAZ'),
    
    -- 4. Sınıf
    ('Bitirme Projesi', 'YAZM401', 'YAZ'),
    
    -- Elektrik-Elektronik Mühendisliği (EEE) dersleri
    -- 1. Sınıf
    ('Devre Analizi', 'EEE101', 'EEE'),
    ('Elektrik Devreleri', 'EEE102', 'EEE'),
    ('Dijital Elektronik', 'EEE103', 'EEE'),
    
    -- 2. Sınıf
    ('Elektronik I', 'EEE201', 'EEE'),
    ('Sinyaller ve Sistemler', 'EEE202', 'EEE'),
    ('Elektromanyetik Alanlar', 'EEE203', 'EEE'),
    
    -- 3. Sınıf
    ('Mikroişlemciler', 'EEE301', 'EEE'),
    ('Güç Elektroniği', 'EEE302', 'EEE'),
    ('Kontrol Sistemleri', 'EEE303', 'EEE'),
    
    -- 4. Sınıf
    ('Bitirme Projesi', 'EEE401', 'EEE'),
    
    -- Genel Dersler (GEN)
    ('Türk Dili', 'GEN101', NULL),
    ('Atatürk İlkeleri', 'GEN102', NULL),
    ('İngilizce', 'GEN201', NULL),
    ('Giriş Dersi', 'GEN301', NULL)
) AS course_data(name, code, dept_code)
LEFT JOIN public.departments d ON d.code = course_data.dept_code
WHERE NOT EXISTS (
    SELECT 1 FROM public.courses c 
    WHERE c.name = course_data.name 
    AND (c.department_id = d.id OR (c.department_id IS NULL AND d.id IS NULL))
)
ON CONFLICT DO NOTHING;

-- 4. Sınıfların grade_level'ını güncelle (eğer yoksa)
UPDATE public.classes
SET grade_level = CAST(SUBSTRING(name FROM '^(\d+)') AS INTEGER)
WHERE grade_level IS NULL 
  AND name ~ '^\d+';

-- 5. Eğer grade_level kolonu yoksa ekle
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'classes' 
        AND column_name = 'grade_level'
    ) THEN
        ALTER TABLE public.classes ADD COLUMN grade_level INTEGER;
        
        UPDATE public.classes
        SET grade_level = CAST(SUBSTRING(name FROM '^(\d+)') AS INTEGER)
        WHERE name ~ '^\d+';
        
        CREATE INDEX IF NOT EXISTS idx_classes_grade_level ON public.classes(grade_level);
    END IF;
END $$;

-- 6. Sonuçları göster
SELECT 
    d.name AS department,
    c.name AS course_name,
    c.code AS course_code,
    COUNT(*) OVER (PARTITION BY d.id) AS total_courses_in_dept
FROM public.courses c
LEFT JOIN public.departments d ON c.department_id = d.id
ORDER BY d.code, c.code;

-- 7. Sınıf seviyelerini göster
SELECT 
    grade_level,
    COUNT(*) AS class_count,
    STRING_AGG(name, ', ' ORDER BY name) AS classes
FROM public.classes
WHERE grade_level IS NOT NULL
GROUP BY grade_level
ORDER BY grade_level;

