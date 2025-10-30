-- Seed example courses per department (id resolved by code)

-- Helper: upsert course if not exists by (name, department)
WITH dept AS (
  SELECT id, code FROM public.departments
)
INSERT INTO public.courses (name, code, department_id)
SELECT x.name, x.code, d.id
FROM (
  VALUES
    -- Yazılım Mühendisliği
    ('Algoritmalar', 'YAZM101', 'YAZ'),
    ('Veri Yapıları', 'YAZM102', 'YAZ'),
    ('Veritabanı Sistemleri', 'YAZM201', 'YAZ'),
    ('Web Programlama', 'YAZM202', 'YAZ'),
    -- Bilgisayar Mühendisliği
    ('Programlamaya Giriş', 'CENG101', 'CENG'),
    ('Sayısal Mantık', 'CENG103', 'CENG'),
    ('İşletim Sistemleri', 'CENG301', 'CENG'),
    ('Ağ Teknolojileri', 'CENG302', 'CENG'),
    -- Elektrik-Elektronik Mühendisliği
    ('Devre Analizi', 'EEE101', 'EEE'),
    ('Elektronik I', 'EEE201', 'EEE'),
    ('Sinyaller ve Sistemler', 'EEE202', 'EEE'),
    -- Endüstri Mühendisliği
    ('Yöneylem Araştırması', 'IE201', 'IE'),
    ('Üretim Planlama', 'IE301', 'IE')
) AS x(name, code, dept_code)
JOIN dept d ON d.code = x.dept_code
WHERE NOT EXISTS (
  SELECT 1 FROM public.courses c 
  WHERE c.name = x.name AND c.department_id = d.id
);

NOTIFY pgrst, 'reload schema';



