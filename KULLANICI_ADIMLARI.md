# Kullanıcının Yapması Gerekenler

## 1. Database Migrations (Supabase SQL Editor)

Supabase Dashboard → SQL Editor'a git ve şu migration'ları sırayla çalıştır:

### Migration 1: `20250116_add_weekly_hours_to_courses.sql`
```sql
-- Add weekly_hours column to courses table
-- This represents how many hours per week a course is taught

-- Add weekly_hours column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'courses' 
        AND column_name = 'weekly_hours'
    ) THEN
        ALTER TABLE public.courses 
        ADD COLUMN weekly_hours INTEGER NOT NULL DEFAULT 2 
        CHECK (weekly_hours > 0 AND weekly_hours <= 10);
        
        COMMENT ON COLUMN public.courses.weekly_hours IS 'Haftalık ders saati sayısı (1-10 arası)';
    END IF;
END $$;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_courses_weekly_hours 
ON public.courses(weekly_hours);

NOTIFY pgrst, 'reload schema';
```

### Migration 2: `20250116_create_course_weekly_sessions.sql`
```sql
-- Create course_weekly_sessions table
-- This table tracks which course sessions have been completed for each week
-- Example: A course with 4 weekly hours will have 4 sessions per week (session_number: 1, 2, 3, 4)

CREATE TABLE IF NOT EXISTS public.course_weekly_sessions (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
    week_number INTEGER NOT NULL CHECK (week_number > 0 AND week_number <= 20),
    session_number INTEGER NOT NULL CHECK (session_number > 0),
    qr_code_id UUID REFERENCES public.qr_codes(id) ON DELETE SET NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE(course_id, week_number, session_number)
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_course_weekly_sessions_course_week 
ON public.course_weekly_sessions(course_id, week_number);

CREATE INDEX IF NOT EXISTS idx_course_weekly_sessions_qr_code 
ON public.course_weekly_sessions(qr_code_id) 
WHERE qr_code_id IS NOT NULL;

-- Disable RLS for easier access (adjust as needed for production)
ALTER TABLE public.course_weekly_sessions DISABLE ROW LEVEL SECURITY;

-- Grant permissions
GRANT SELECT, INSERT, UPDATE, DELETE ON public.course_weekly_sessions TO anon, authenticated;
GRANT USAGE, SELECT ON SEQUENCE public.course_weekly_sessions_id_seq TO anon, authenticated;

COMMENT ON TABLE public.course_weekly_sessions IS 'Haftalık ders oturumlarını takip eder. Her ders için haftalık saat sayısı kadar oturum oluşturulur.';
COMMENT ON COLUMN public.course_weekly_sessions.session_number IS 'Hafta içindeki oturum numarası (1, 2, 3, 4... weekly_hours kadar)';
COMMENT ON COLUMN public.course_weekly_sessions.qr_code_id IS 'Bu oturum için oluşturulan QR kod ID (null ise henüz işlenmemiş)';

NOTIFY pgrst, 'reload schema';
```

### Migration 3: `20250116_add_session_number_to_attendances.sql`
```sql
-- Add session_number column to attendances table
-- This allows tracking attendance for each session separately

-- Add session_number column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'attendances' 
        AND column_name = 'session_number'
    ) THEN
        ALTER TABLE public.attendances 
        ADD COLUMN session_number INTEGER;
        
        COMMENT ON COLUMN public.attendances.session_number IS 'Haftalık ders oturum numarası (1, 2, 3, 4...). NULL ise tek oturum varsayılır (geriye dönük uyumluluk)';
    END IF;
END $$;

-- Drop old unique constraint if it exists
DROP INDEX IF EXISTS uq_attendance_assignment_student_day;

-- Create new unique constraint that includes session_number
-- This prevents duplicate attendance for the same course/week/session/student/day
CREATE UNIQUE INDEX IF NOT EXISTS uq_attendance_course_week_session_student_day
ON public.attendances (
  course_id,
  week_number,
  COALESCE(session_number, 1), -- Treat NULL as 1 for backward compatibility
  student_id,
  (date_trunc('day', marked_at))
);

-- Create index for faster queries by session_number
CREATE INDEX IF NOT EXISTS idx_attendances_session_number 
ON public.attendances(session_number) 
WHERE session_number IS NOT NULL;

COMMENT ON TABLE public.attendances IS 'Yoklama kayıtları. Her oturum için ayrı kayıt oluşturulur.';

NOTIFY pgrst, 'reload schema';
```

**ÖNEMLİ:** Her migration'ı tek tek çalıştır ve hata olmadığından emin ol!

---

## 2. Edge Functions Deploy (Supabase Dashboard)

Supabase Dashboard → Edge Functions'a git:

### Function 1: `get-weekly-sessions` (YENİ)
1. "Create a new function" butonuna tıkla
2. Function name: `get-weekly-sessions`
3. `supabase/functions/get-weekly-sessions/index.ts` dosyasının içeriğini kopyala-yapıştır
4. "Deploy" butonuna tıkla
5. **"Require Authorization" seçeneğini KAPALI yap** (Settings'ten)

### Function 2: `create-qr` (GÜNCELLEME)
1. Mevcut `create-qr` function'ını bul
2. "Edit" butonuna tıkla
3. `supabase/functions/create-qr/index.ts` dosyasının **güncellenmiş** içeriğini kopyala-yapıştır
4. "Deploy" butonuna tıkla

### Function 3: `validate-qr` (GÜNCELLEME)
1. Mevcut `validate-qr` function'ını bul
2. "Edit" butonuna tıkla
3. `supabase/functions/validate-qr/index.ts` dosyasının **güncellenmiş** içeriğini kopyala-yapıştır
4. "Deploy" butonuna tıkla

---

## 3. Mevcut Derslere weekly_hours Ekle (Opsiyonel)

Eğer veritabanında mevcut dersler varsa, onlara `weekly_hours` değeri atamak isteyebilirsin:

```sql
-- Tüm derslere varsayılan olarak 2 saat ata
UPDATE public.courses 
SET weekly_hours = 2 
WHERE weekly_hours IS NULL OR weekly_hours = 0;

-- Veya belirli derslere özel saat ata
UPDATE public.courses 
SET weekly_hours = 4 
WHERE name = 'Algoritmalar';

UPDATE public.courses 
SET weekly_hours = 3 
WHERE name = 'Veri Yapıları';
```

---

## Kontrol Listesi

- [ ] Migration 1 çalıştırıldı (weekly_hours)
- [ ] Migration 2 çalıştırıldı (course_weekly_sessions)
- [ ] Migration 3 çalıştırıldı (session_number)
- [ ] `get-weekly-sessions` function deploy edildi
- [ ] `create-qr` function güncellendi ve deploy edildi
- [ ] `validate-qr` function güncellendi ve deploy edildi
- [ ] Mevcut derslere weekly_hours değeri atandı (opsiyonel)

---

## Hata Durumunda

Eğer migration'larda hata alırsan:
1. Hata mesajını kopyala
2. Bana gönder
3. Birlikte çözelim

Eğer function deploy'da hata alırsan:
1. Function log'larına bak
2. Hata mesajını kopyala
3. Bana gönder

---

## Tamamlandığında

Tüm adımları tamamladığında bana "tamamlandı" yaz, ben de Android UI güncellemesine devam edeyim! 🚀



