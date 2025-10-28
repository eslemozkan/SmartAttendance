# 🧪 Edge Function Test

## Edge Function Deploy Edildi mi?

Tarayıcıda test et: https://oubvhffqbsxsnbtinzbl.functions.supabase.co/student-signup

Eğer **404** görürsen → Deploy edilmemiş!
Eğer JSON response dönerse → Deploy edilmiş!

---

## 📝 Edge Function List'i Kontrol Et

1. Supabase Dashboard → Edge Functions
2. `student-signup` listede var mı?
3. Status nedir? (Active / Inactive)
4. Invoke URL nedir?

---

## ⚠️ Anon Key Güncel mi?

Şu anda kullanılan: 
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im91YnZoZmZxYnN4c25idGluemJsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA4ODk4NzksImV4cCI6MjA3NjQ2NTg3OX0.kn6pYhbOFWBywNrenjZI9ZUPpOnwKugbIqZkOFcGrnI
```

**Supabase Dashboard'da kontrol et:**
- Settings → API
- anon/public key doğru mu?
- Service Role key eklendiyse Edge Function'a environment variable olarak eklendi mi?

---

## 🔧 Yapılacaklar:

1. Edge Function deploy edildi mi kontrol et
2. Environment variable eklendi mi kontrol et
3. Anon key güncel mi kontrol et
4. Android'de tekrar test et

