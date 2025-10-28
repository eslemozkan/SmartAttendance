# 🔑 Service Role Key Nasıl Alınır?

## Adımlar:

1. **Supabase Dashboard'a Git**: https://supabase.com/dashboard/project/oubvhffqbsxsnbtinzbl

2. **Settings'e Git**: Sol menüden "Settings" → "API"

3. **Service Role Key'i Kopyala**: 
   - "service_role" key'ini bul
   - **SAKIN SEKMEKEY İLE PAYLAŞMA!** (Güvenlik riski!)
   - Kopyala

4. **Edge Function'a Ekle**:
   - Supabase Dashboard → Edge Functions → student-signup
   - Settings → Environment variables
   - Yeni variable ekle:
     - Name: `SUPABASE_SERVICE_ROLE_KEY`
     - Value: (kopyaladığın key)

---

## Alternatif: Koda Direkt Ekle

Eğer environment variable çalışmıyorsa, kodu güncelle ve **hardcode et** (sadece test için):

```typescript
const serviceKey = "PASTE_YOUR_SERVICE_ROLE_KEY_HERE";
```

⚠️ **GÜVENLİK UYARISI**: 
- Production'da asla hardcode etme!
- Service Role Key çok güçlü, kimseyle paylaşma!

