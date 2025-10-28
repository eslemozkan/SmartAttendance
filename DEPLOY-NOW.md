# 🚀 ŞİMDİ NE YAPMALIYIZ?

## 1️⃣ Supabase Dashboard'a Git
https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl/edge-functions

## 2️⃣ "Deploy a new function" butonuna bas

## 3️⃣ Doldur:
- **Function name**: `student-signup`
- **Function code**: Şu dosyanın içeriğini kopyala yapıştır:
  - Dosya: `supabase/functions/student-signup/index.ts`

## 4️⃣ Environment Variables Ekle:
Dashboard'da Settings → Edge Functions → Environment variables

İKİ DEĞİŞKEN EKLE:
```
SUPABASE_URL=https://oubvhffqbsxsnbtinzbl.supabase.co
SUPABASE_SERVICE_ROLE_KEY=<service_role_key_yu_here>
```

Service Role Key'i nereden al:
Dashboard → Settings → API → service_role key (kopyala)

## 5️⃣ Android'de Test Et:
Uygulamayı başlat → Signup ekranından kayıt ol

---

### 🔥 ÖNEMLİ: Deploy edene kadar "network_error" alacaksın!
### ✅ Deploy edince Android'de test et!

