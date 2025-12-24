# Test Derslerini Temizleme

## Durum
Web-admin panelinde test amaçlı eklenmiş dersler görünüyor:
- "fgdfgdf (dfdfgf)"
- "a (aaaa)"
- "asad (aswdsa)"
- "e (e)"
- "gg (gg)"
- "ai (l)"
- "qq (qq)"
- "r (rr)"
- "s (s)"
- "asas (sasds)"
- "t (t)"
- "ewewr (wew)"

## Çözüm

### 1. Kontrol Et
Önce hangi derslerin silineceğini görmek için SQL çalıştır:

```sql
SELECT 
    id,
    name,
    code,
    department_id,
    created_at
FROM public.courses
WHERE 
    LENGTH(TRIM(name)) <= 2
    OR
    (code IS NOT NULL AND LENGTH(TRIM(code)) <= 2)
    OR
    LOWER(name) IN ('a', 'e', 'r', 's', 't', 'gg', 'qq', 'ai', 'l')
    OR
    name ~ '^(.)\1+$'
    OR
    name ~ '^[a-z]{1,3}$'
ORDER BY created_at DESC;
```

### 2. Manuel Temizleme (Önerilen)
Web-admin panelinden:
1. Her test dersinin yanındaki silme (🗑️) ikonuna tıkla
2. Onayla
3. İlişkili kayıtlar otomatik silinir (cascade)

### 3. Otomatik Temizleme (Dikkatli!)
Eğer çok fazla test dersi varsa, SQL scripti kullan:

**DİKKAT:** Bu işlem geri alınamaz! Önce yukarıdaki SELECT ile kontrol et.

`supabase/migrations/20250116_cleanup_test_courses.sql` dosyasındaki DELETE komutlarının yorum satırlarını kaldır ve çalıştır.

---

## Alternatif: Belirli Dersleri Manuel Silme

Supabase Dashboard'dan:
1. Table Editor → `courses` tablosuna git
2. Filtrele: `name` içinde "fgdfgdf", "a", "asad", vb.
3. Seç ve sil

---

## Önleme
Gelecekte test dersleri eklememek için:
- Test ortamında çalışırken dikkatli ol
- Gerçek ders isimleri kullan (örn: "Test Dersi 1" yerine "Algoritma Test")
- Test sonrası temizle



