-- Admin kullanıcısı oluşturma ve test etme
-- Bu SQL komutlarını Supabase SQL Editor'da çalıştırın

-- 1. Önce mevcut admin kullanıcısını kontrol et
SELECT * FROM admins WHERE email = 'admin@university.edu';

-- 2. Eğer yoksa, yeni admin kullanıcısı oluştur
INSERT INTO admins (email, password_hash, full_name, role) 
VALUES (
  'admin@university.edu', 
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
  'Sistem Yöneticisi', 
  'super_admin'
) 
ON CONFLICT (email) DO NOTHING;

-- 3. Admin kullanıcısını kontrol et
SELECT * FROM admins;

-- 4. Supabase Auth'da da kullanıcı oluştur (gerekirse)
-- Bu kısmı Supabase Dashboard > Authentication > Users kısmından manuel olarak yapabilirsiniz
-- Email: admin@university.edu
-- Password: admin123


