# Student Attendance API Fix

## Sorun Analizi

1. **RLS/Permission Sorunu**: `course_class_assignments` tablosunda RLS açık ve sadece admin'lere erişim var. `anon` ve `authenticated` rolleri erişemiyor.

2. **Veri Tipi Uyumsuzluğu**: 
   - `course_class_assignments.course_id` → UUID (admin-schema.sql'de)
   - `courses.id` → BIGINT (gerçek veritabanında)
   - Bu uyumsuzluk join işlemlerinde sorun yaratıyor.

3. **Timeout Sorunu**: RLS nedeniyle erişim reddediliyor, bu da timeout'a neden oluyor.

## Çözüm Adımları

### 1. Diagnostic Sorguları Çalıştır

Önce `check_student_attendance_issues.sql` dosyasını Supabase SQL Editor'de çalıştırın. Bu sorgular:
- Tablo yapılarını kontrol eder
- RLS durumunu gösterir
- Veri tipi uyumsuzluklarını tespit eder
- Örnek verileri gösterir

### 2. Migration'ı Uygula

`supabase/migrations/20250115_fix_course_class_assignments_permissions.sql` dosyasını çalıştırın:

```bash
# Supabase CLI ile
supabase db push

# Veya Supabase Dashboard > SQL Editor'de direkt çalıştırın
```

Bu migration:
- `course_class_assignments` tablosunda RLS'i kapatır
- `anon` ve `authenticated` rolleri için SELECT izni verir
- Veri tipi uyumsuzluğunu handle eden bir view oluşturur
- `courses` ve `classes` tablolarına da erişim sağlar

### 3. Alternatif: View Kullanımı

Eğer veri tipi uyumsuzluğu devam ederse, API'yi view kullanacak şekilde güncelleyin:

```kotlin
// student_course_assignments view'ını kullan
val assignmentsUrl = "$restBaseUrl/student_course_assignments?select=course_id_bigint,course_name,course_code&class_id=eq.$classId"
```

### 4. Test

1. Uygulamayı çalıştırın
2. "Yoklama Durumumu Kontrol Et" butonuna tıklayın
3. Logcat'te şu logları kontrol edin:
   - "Step 4a: Getting course assignments"
   - "Assignments response code"
   - "Parsed X course assignments"

## Beklenen Sonuç

- `course_class_assignments` tablosuna erişim başarılı olmalı
- Timeout hatası olmamalı
- Öğrencinin dersleri listelenmeli
- QR kodları ve yoklama durumları gösterilmeli

## Sorun Devam Ederse

1. `check_student_attendance_issues.sql` çıktısını kontrol edin
2. Veri tipi uyumsuzluğu varsa, `course_class_assignments.course_id` tipini `courses.id` ile eşleştirin
3. RLS politikalarını kontrol edin: `SELECT * FROM pg_policies WHERE tablename = 'course_class_assignments'`
4. Permissions'ı kontrol edin: `SELECT * FROM information_schema.role_table_grants WHERE table_name = 'course_class_assignments'`

