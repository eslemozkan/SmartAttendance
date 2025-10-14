# SmartAttendance

## QR Kod Süreli Yoklama Sistemi

Bu proje, öğretmenin belirlediği dakika cinsinden süre ile geçerli olacak QR kodlar üretir ve öğrencilerin bu kodu okutarak yoklama almasını sağlar.

### Veritabanı

Supabase migration ile `qr_codes` tablosu eklendi:

```
id uuid PK, assignment_id bigint, created_at timestamptz, expire_after_minutes int>0, is_active boolean
```

RLS: Varsayılan olarak yalnızca service role tüm işlemleri yapabilir.

Migration dosyası: `supabase/migrations/20251013_add_qr_codes.sql`

### Edge Functions

- `create-qr`: Verilen `assignment_id` ve `expire_after_minutes` ile kayıt oluşturur ve QR payload döner.
- `validate-qr`: Öğrenciden gelen QR payload (`assignment_id, created_at, expire_after, student_id`) ve zaman damgasını doğrular, geçerliyse yoklama kaydı ekler.

Fonksiyon yolları:
- `supabase/functions/create-qr/index.ts`
- `supabase/functions/validate-qr/index.ts`

Gerekli ortam değişkenleri:
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`

### API Kullanımı

1) QR oluştur (öğretmen)

İstek:

```http
POST /functions/v1/create-qr
Content-Type: application/json

{
  "assignment_id": 12,
  "expire_after_minutes": 15
}
```

Yanıt:

```json
{
  "id": "<qr_record_uuid>",
  "qr": {
    "assignment_id": 12,
    "created_at": "2025-10-10T06:30:00.000Z",
    "expire_after": 15
  }
}
```

Bu `qr` payload'ı QR koda encode edilmelidir (JSON metni yeterlidir).

2) QR doğrula ve yoklama al (öğrenci)

İstek:

```http
POST /functions/v1/validate-qr
Content-Type: application/json

{
  "assignment_id": 12,
  "created_at": "2025-10-10T06:30:00.000Z",
  "expire_after": 15,
  "student_id": "student-uuid-or-number"
}
```

Olası yanıtlar:
- 200 `{ "ok": true }`
- 410 `{ "error": "Bu yoklamanın süresi dolmuş" }`
- 404 `{ "error": "QR bulunamadı veya pasif" }`
- 409 `{ "error": "Bu öğrenci için zaten yoklama alınmış" }`

### UI Önerileri

- Öğretmen ekranında süre seçenekleri: 5, 10, 15, 30 dk.
- Geri sayım göstergesi: "Bu yoklama 03:24 içinde bitecek".
- Süre bitince "Yeni QR oluştur" butonuna dönüşür.

### Notlar

- `attendances` tablosu bu repoda tanımlı değildir; kurulumda var olduğu ve `assignment_id, student_id` ile yoklama satırı yazılabileceği varsayılmaktadır.
- Duplicateden kaçınmak için `attendances` üzerinde uygun unique kısıt önerilir: `(assignment_id, student_id, date_trunc('day', marked_at))` vb.

## Attendances Şeması

Migration ile eklenen tablo: `supabase/migrations/20251013_add_attendances.sql`

```
id uuid PK,
assignment_id bigint not null,
student_id text not null,
marked_at timestamptz default now(),
method text default 'qr',
meta jsonb default '{}'
```

Kurallar:
- Gün bazında tekil yoklama: `(assignment_id, student_id, date_trunc('day', marked_at))` unique index.
- Service role ile insert; authenticated kullanıcılar kendi kayıtlarını okuyabilir (policy düzenlenebilir).