# 🔐 Edge Function Secret Kurulumu

## ❗ ÖNEMLİ: SUPABASE_SERVICE_ROLE_KEY Eksik!

Edge function `reset-password` çalışmıyor çünkü `SUPABASE_SERVICE_ROLE_KEY` secret'ı eksik.

## ✅ Çözüm: Secret Ekle

### Adım 1: Service Role Key'i Al

1. **Supabase Dashboard'a git:**
   - https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Settings → API** bölümüne git

3. **Service Role Key**'i kopyala
   - ⚠️ **DİKKAT:** Bu key çok önemli! Kimseyle paylaşma!

### Adım 2: Edge Function'a Secret Ekle

#### Yöntem A: Supabase Dashboard'dan (Kolay)

1. **Edge Functions → reset-password** bölümüne git
2. **Settings** sekmesine tıkla
3. **Secrets** bölümüne git
4. **Add secret** butonuna tıkla
5. Şu bilgileri gir:
   - **Key:** `SUPABASE_SERVICE_ROLE_KEY`
   - **Value:** Service Role Key (1. adımda kopyaladığın)
6. **Save** butonuna tıkla

#### Yöntem B: Supabase CLI ile

```bash
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=your_service_role_key_here --project-ref oubvhffqbsxsnbtinzbl
```

### Adım 3: Edge Function'ı Yeniden Deploy Et

Secret ekledikten sonra edge function'ı yeniden deploy et:

```bash
supabase functions deploy reset-password
```

**VEYA** Supabase Dashboard'dan:
- Edge Functions → reset-password → **Deploy** butonuna tıkla

## ✅ Kontrol Et

1. **Secret eklendi mi?**
   - Edge Functions → reset-password → Settings → Secrets
   - `SUPABASE_SERVICE_ROLE_KEY` listede var mı?

2. **Edge function deploy edildi mi?**
   - Edge Functions → reset-password
   - Status: **Active** olmalı

3. **Test et:**
   - Android uygulamasında "Şifremi Unuttum" özelliğini kullan
   - Email adresini gir
   - Hata mesajı gelmemeli

## 🔍 Sorun Giderme

### ❌ "Kullanıcı kontrolü yapılamadı" hatası
**Sebep:** `SUPABASE_SERVICE_ROLE_KEY` secret'ı eksik veya yanlış
**Çözüm:** Yukarıdaki adımları takip et

### ❌ "Missing Supabase service role key" hatası
**Sebep:** Secret eklenmemiş
**Çözüm:** Adım 2'yi tekrarla

### ❌ Edge function çalışmıyor
**Sebep:** Deploy edilmemiş olabilir
**Çözüm:** Adım 3'ü tekrarla

---

## 📝 Notlar

- Service Role Key çok güçlü bir key'dir. Sadece edge function'larda kullanılmalıdır.
- Secret'lar edge function'lar arasında paylaşılmaz. Her function için ayrı ayrı eklenmelidir.
- Secret ekledikten sonra edge function'ı yeniden deploy etmek gerekir.


