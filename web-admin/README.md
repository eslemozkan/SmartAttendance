# Smart Attendance Admin Panel

Web tabanlı admin paneli - Android uygulamasıyla entegre dinamik yoklama sistemi.

## 🚀 Özellikler

### 👥 Kullanıcı Yönetimi
- **Öğrenci Yönetimi**: Öğrenci ekleme, düzenleme, silme
- **Öğretmen Yönetimi**: Öğretmen ekleme, düzenleme, silme
- **Admin Yönetimi**: Sistem yöneticileri

### 📚 Ders ve Sınıf Yönetimi
- **Ders Yönetimi**: Ders ekleme, düzenleme, silme
- **Sınıf Yönetimi**: Sınıf oluşturma ve öğrenci atamaları
- **Ders-Sınıf Atamaları**: Hangi dersin hangi sınıfa atandığını belirleme
- **Öğretmen Atamaları**: Derslere öğretmen atama

### 📊 Raporlama ve Analiz
- **Detaylı Yoklama Raporları**: Öğrenci bazında yoklama durumu
- **Sınıf Bazında Analiz**: Sınıf yoklama oranları
- **Ders Bazında Analiz**: Ders yoklama istatistikleri
- **Excel/CSV Export**: Raporları dışa aktarma

### 🔐 Güvenlik
- **Admin Authentication**: Güvenli giriş sistemi
- **Role-based Access**: Yetki bazlı erişim kontrolü
- **Data Validation**: Veri doğrulama ve güvenlik

## 🛠️ Teknoloji Stack

- **Frontend**: Next.js 14 + React 18 + TypeScript
- **Styling**: Tailwind CSS (Academic tema)
- **Backend**: Supabase (PostgreSQL + Auth + Real-time)
- **Icons**: Lucide React
- **Charts**: Recharts

## 📁 Proje Yapısı

```
web-admin/
├── src/
│   ├── app/                    # Next.js App Router
│   │   ├── page.tsx           # Ana dashboard
│   │   ├── login/             # Giriş sayfası
│   │   └── admin/             # Admin sayfaları
│   │       ├── students/      # Öğrenci yönetimi
│   │       ├── teachers/      # Öğretmen yönetimi
│   │       ├── courses/       # Ders yönetimi
│   │       ├── classes/       # Sınıf yönetimi
│   │       └── reports/       # Raporlar
│   ├── components/            # Reusable bileşenler
│   ├── lib/                   # Utilities ve konfigürasyon
│   └── styles/                # CSS dosyaları
├── supabase/                  # Veritabanı şemaları
└── public/                    # Static dosyalar
```

## 🚀 Kurulum

### 1. Bağımlılıkları Yükle
```bash
cd web-admin
npm install
```

### 2. Supabase Veritabanını Kur
```bash
# Supabase CLI ile şemayı uygula
supabase db reset
# veya SQL dosyasını manuel olarak çalıştır
psql -h your-host -U your-user -d your-db -f supabase/admin-schema.sql
```

### 3. Environment Variables
`.env.local` dosyası oluştur:
```env
NEXT_PUBLIC_SUPABASE_URL=https://oubvhffqbsxsnbtinzbl.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Development Server'ı Başlat
```bash
npm run dev
```

Admin paneli `http://localhost:3000` adresinde çalışacak.

## 🔑 Demo Hesap

- **Email**: admin@university.edu
- **Şifre**: admin123

## 📱 Android Entegrasyonu

Admin panelinde yapılan değişiklikler otomatik olarak Android uygulamasına yansır:

1. **Öğrenci Ekleme**: Yeni öğrenciler Android'de giriş yapabilir
2. **Ders Atamaları**: Öğretmenler sadece atandıkları dersleri görebilir
3. **Sınıf Yönetimi**: Yoklama listeleri dinamik olarak güncellenir
4. **Raporlama**: Android'den dışa aktarılan raporlar admin panelinde görüntülenebilir

## 🎨 Tasarım Sistemi

Academic tema Android uygulamasıyla tutarlı:
- **Primary**: #1976D2 (Mavi)
- **Secondary**: #424242 (Koyu gri)
- **Success**: #4CAF50 (Yeşil)
- **Error**: #F44336 (Kırmızı)
- **Background**: #FAFAFA (Açık gri)

## 📊 Veritabanı Şeması

### Ana Tablolar
- `admins`: Admin kullanıcıları
- `departments`: Bölümler
- `classes`: Sınıflar
- `students`: Öğrenciler
- `profiles`: Öğretmenler (mevcut)
- `courses`: Dersler (mevcut)
- `course_class_assignments`: Ders-sınıf atamaları
- `attendances`: Yoklama kayıtları (genişletilmiş)

### İlişkiler
- Öğrenci → Sınıf → Bölüm
- Ders → Sınıf → Öğretmen
- Yoklama → Öğrenci → Ders → Sınıf

## 🔄 Geliştirme Roadmap

### Faz 1: Temel Admin Panel ✅
- [x] Admin authentication
- [x] Dashboard
- [x] Öğrenci yönetimi
- [x] Temel CRUD işlemleri

### Faz 2: Gelişmiş Özellikler 🚧
- [ ] Öğretmen yönetimi
- [ ] Ders yönetimi
- [ ] Sınıf yönetimi
- [ ] Ders-sınıf atamaları

### Faz 3: Raporlama 📊
- [ ] Detaylı raporlar
- [ ] Grafik ve analizler
- [ ] Excel/PDF export
- [ ] Real-time dashboard

### Faz 4: Gelişmiş Özellikler 🚀
- [ ] Bulk operations
- [ ] Advanced search/filter
- [ ] Notification system
- [ ] Audit logs

## 🤝 Katkıda Bulunma

1. Fork yapın
2. Feature branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Commit yapın (`git commit -m 'Add amazing feature'`)
4. Push yapın (`git push origin feature/amazing-feature`)
5. Pull Request oluşturun

## 📄 Lisans

Bu proje MIT lisansı altında lisanslanmıştır.


