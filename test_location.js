// Konum Kontrolü Test Scripti
// Haversine formülü ile mesafe hesaplama testi

function calculateDistance(lat1, lon1, lat2, lon2) {
    const R = 6371000; // Earth radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in meters
}

console.log("=".repeat(60));
console.log("KONUM KONTROLÜ TEST SENARYOLARI");
console.log("=".repeat(60));

// Test Senaryoları
const maxDistance = 30; // 30 metre

// Senaryo 1: Aynı konum (0 metre) - BAŞARILI OLMALI
const teacherLat1 = 39.123456;
const teacherLon1 = 35.123456;
const studentLat1 = 39.123456;
const studentLon1 = 35.123456;
const distance1 = calculateDistance(teacherLat1, teacherLon1, studentLat1, studentLon1);
console.log("\n✅ Senaryo 1: Aynı Konum");
console.log(`   Öğretmen: ${teacherLat1}, ${teacherLon1}`);
console.log(`   Öğrenci:  ${studentLat1}, ${studentLon1}`);
console.log(`   Mesafe: ${distance1.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance1 <= maxDistance ? '✅ BAŞARILI (Yoklama alınmalı)' : '❌ BAŞARISIZ'}`);

// Senaryo 2: 10 metre uzaklık - BAŞARILI OLMALI
// 1 derece ≈ 111 km, 10 metre ≈ 0.00009 derece
const teacherLat2 = 39.123456;
const teacherLon2 = 35.123456;
const studentLat2 = 39.123456 + 0.00009; // ~10 metre kuzey
const studentLon2 = 35.123456;
const distance2 = calculateDistance(teacherLat2, teacherLon2, studentLat2, studentLon2);
console.log("\n✅ Senaryo 2: 10 Metre Uzaklık");
console.log(`   Öğretmen: ${teacherLat2}, ${teacherLon2}`);
console.log(`   Öğrenci:  ${studentLat2.toFixed(6)}, ${studentLon2}`);
console.log(`   Mesafe: ${distance2.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance2 <= maxDistance ? '✅ BAŞARILI (Yoklama alınmalı)' : '❌ BAŞARISIZ'}`);

// Senaryo 3: 25 metre uzaklık - BAŞARILI OLMALI
// 25 metre ≈ 0.000225 derece
const teacherLat3 = 39.123456;
const teacherLon3 = 35.123456;
const studentLat3 = 39.123456 + 0.000225; // ~25 metre kuzey
const studentLon3 = 35.123456;
const distance3 = calculateDistance(teacherLat3, teacherLon3, studentLat3, studentLon3);
console.log("\n✅ Senaryo 3: 25 Metre Uzaklık (Sınırda)");
console.log(`   Öğretmen: ${teacherLat3}, ${teacherLon3}`);
console.log(`   Öğrenci:  ${studentLat3.toFixed(6)}, ${studentLon3}`);
console.log(`   Mesafe: ${distance3.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance3 <= maxDistance ? '✅ BAŞARILI (Yoklama alınmalı)' : '❌ BAŞARISIZ'}`);

// Senaryo 4: 30 metre uzaklık (tam sınır) - BAŞARILI OLMALI
// 30 metre ≈ 0.00027 derece
const teacherLat4 = 39.123456;
const teacherLon4 = 35.123456;
const studentLat4 = 39.123456 + 0.00027; // ~30 metre kuzey
const studentLon4 = 35.123456;
const distance4 = calculateDistance(teacherLat4, teacherLon4, studentLat4, studentLon4);
console.log("\n⚠️  Senaryo 4: 30 Metre Uzaklık (Tam Sınır)");
console.log(`   Öğretmen: ${teacherLat4}, ${teacherLon4}`);
console.log(`   Öğrenci:  ${studentLat4.toFixed(6)}, ${studentLon4}`);
console.log(`   Mesafe: ${distance4.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance4 <= maxDistance ? '✅ BAŞARILI (Yoklama alınmalı)' : '❌ BAŞARISIZ'}`);

// Senaryo 5: 35 metre uzaklık - BAŞARISIZ OLMALI
// 35 metre ≈ 0.000315 derece
const teacherLat5 = 39.123456;
const teacherLon5 = 35.123456;
const studentLat5 = 39.123456 + 0.000315; // ~35 metre kuzey
const studentLon5 = 35.123456;
const distance5 = calculateDistance(teacherLat5, teacherLon5, studentLat5, studentLon5);
console.log("\n❌ Senaryo 5: 35 Metre Uzaklık (Sınırı Aşıyor)");
console.log(`   Öğretmen: ${teacherLat5}, ${teacherLon5}`);
console.log(`   Öğrenci:  ${studentLat5.toFixed(6)}, ${studentLon5}`);
console.log(`   Mesafe: ${distance5.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance5 <= maxDistance ? '✅ BAŞARILI' : '❌ BAŞARISIZ (Yoklama ALINMAMALI - Konum çok uzak)'}`);

// Senaryo 6: 100 metre uzaklık - BAŞARISIZ OLMALI
// 100 metre ≈ 0.0009 derece
const teacherLat6 = 39.123456;
const teacherLon6 = 35.123456;
const studentLat6 = 39.123456 + 0.0009; // ~100 metre kuzey
const studentLon6 = 35.123456;
const distance6 = calculateDistance(teacherLat6, teacherLon6, studentLat6, studentLon6);
console.log("\n❌ Senaryo 6: 100 Metre Uzaklık (Çok Uzak)");
console.log(`   Öğretmen: ${teacherLat6}, ${teacherLon6}`);
console.log(`   Öğrenci:  ${studentLat6.toFixed(6)}, ${studentLon6}`);
console.log(`   Mesafe: ${distance6.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance6 <= maxDistance ? '✅ BAŞARILI' : '❌ BAŞARISIZ (Yoklama ALINMAMALI - Konum çok uzak)'}`);

// Senaryo 7: Gerçek koordinatlar (Elazığ örneği)
// Elazığ merkez: ~39.2188, 38.6750
const teacherLat7 = 39.2188;
const teacherLon7 = 38.6750;
const studentLat7 = 39.2188 + 0.00027; // ~30 metre kuzey
const studentLon7 = 38.6750;
const distance7 = calculateDistance(teacherLat7, teacherLon7, studentLat7, studentLon7);
console.log("\n📍 Senaryo 7: Gerçek Koordinatlar (Elazığ - 30m)");
console.log(`   Öğretmen: ${teacherLat7}, ${teacherLon7}`);
console.log(`   Öğrenci:  ${studentLat7.toFixed(6)}, ${studentLon7}`);
console.log(`   Mesafe: ${distance7.toFixed(2)} metre`);
console.log(`   Sonuç: ${distance7 <= maxDistance ? '✅ BAŞARILI (Yoklama alınmalı)' : '❌ BAŞARISIZ'}`);

console.log("\n" + "=".repeat(60));
console.log("TEST ÖZETİ:");
console.log("=".repeat(60));
console.log(`✅ Senaryo 1-4: Başarılı olmalı (0-30m arası)`);
console.log(`❌ Senaryo 5-6: Başarısız olmalı (30m'den fazla)`);
console.log(`📍 Senaryo 7: Gerçek koordinat testi`);
console.log("\n💡 NOT: Bu testler mesafe hesaplama fonksiyonunu doğrular.");
console.log("   Gerçek test için validate-qr edge function'ını test etmelisin.");
console.log("=".repeat(60));



