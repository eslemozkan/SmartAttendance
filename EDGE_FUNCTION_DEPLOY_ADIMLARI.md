# Edge Function Deploy Adımları

## 1. Yeni Function: `get-weekly-sessions`

### Adımlar:
1. Supabase Dashboard'a git: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions
2. **"Create a new function"** veya **"New Function"** butonuna tıkla
3. Function name: `get-weekly-sessions` yaz
4. `supabase/functions/get-weekly-sessions/index.ts` dosyasının **tam içeriğini** kopyala
5. Function editor'a yapıştır
6. **"Deploy"** butonuna tıkla
7. Deploy tamamlandıktan sonra:
   - Function'ın yanındaki **"..."** (üç nokta) menüsüne tıkla
   - **"Settings"** veya **"Configure"** seçeneğine tıkla
   - **"Require Authorization"** seçeneğini **KAPALI** yap (OFF)
   - Kaydet

---

## 2. Güncelleme: `create-qr`

### Adımlar:
1. Supabase Dashboard → Edge Functions'a git
2. Mevcut **`create-qr`** function'ını bul
3. **"Edit"** veya **"..."** menüsünden **"Edit"** seçeneğine tıkla
4. Mevcut kodu **tamamen sil**
5. `supabase/functions/create-qr/index.ts` dosyasının **tam içeriğini** kopyala
6. Function editor'a yapıştır
7. **"Deploy"** butonuna tıkla
8. Deploy tamamlandığında kontrol et (hata olmamalı)

---

## 3. Güncelleme: `validate-qr`

### Adımlar:
1. Supabase Dashboard → Edge Functions'a git
2. Mevcut **`validate-qr`** function'ını bul
3. **"Edit"** veya **"..."** menüsünden **"Edit"** seçeneğine tıkla
4. Mevcut kodu **tamamen sil**
5. `supabase/functions/validate-qr/index.ts` dosyasının **tam içeriğini** kopyala
6. Function editor'a yapıştır
7. **"Deploy"** butonuna tıkla
8. Deploy tamamlandığında kontrol et (hata olmamalı)

---

## Kontrol Listesi

- [ ] `get-weekly-sessions` function oluşturuldu ve deploy edildi
- [ ] `get-weekly-sessions` için "Require Authorization" KAPALI
- [ ] `create-qr` function güncellendi ve deploy edildi
- [ ] `validate-qr` function güncellendi ve deploy edildi

---

## Hata Durumunda

Eğer deploy sırasında hata alırsan:
1. Function log'larına bak (her function'ın yanında "Logs" butonu var)
2. Hata mesajını kopyala
3. Bana gönder, birlikte çözelim

---

## Deploy Sonrası Test (Opsiyonel)

Deploy tamamlandıktan sonra test edebilirsin:

### Test 1: get-weekly-sessions
```bash
# PowerShell'de test et
$body = @{
    course_id = 1
    week_number = 1
} | ConvertTo-Json

Invoke-WebRequest -Uri "https://oubvhffqbsxsnbtinzbl.supabase.co/functions/v1/get-weekly-sessions" `
    -Method POST `
    -Headers @{
        "Content-Type" = "application/json"
        "apikey" = "YOUR_ANON_KEY"
    } `
    -Body $body
```

---

## Tamamlandığında

Tüm function'ları deploy ettikten sonra bana "tamamlandı" yaz, ben de Android UI güncellemesine devam edeyim! 🚀



