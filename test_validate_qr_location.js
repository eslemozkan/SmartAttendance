// validate-qr Edge Function Konum Kontrolü Test Scripti
// Server-side mesafe hesaplama mantığını test eder

function calculateDistanceHaversine(lat1, lon1, lat2, lon2) {
    // validate-qr/index.ts'deki aynı formül
    const R = 6371000; // Earth radius in meters
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c; // Distance in meters
}

function testLocationCheck(teacherLat, teacherLon, studentLat, studentLon, scenarioName) {
    const maxDistance = 30; // 30 metres (validate-qr'da tanımlı)
    
    // validate-qr mantığı: Eğer hem öğretmen hem öğrenci konumu varsa kontrol et
    if (teacherLat != null && teacherLon != null && 
        studentLat != null && studentLon != null) {
        const distance = calculateDistanceHaversine(teacherLat, teacherLon, studentLat, studentLon);
        
        if (distance > maxDistance) {
            return {
                passed: false,
                distance: distance.toFixed(2),
                message: `❌ BAŞARISIZ: Öğrenci ${distance.toFixed(2)}m uzakta (max: ${maxDistance}m) - Yoklama ALINMAMALI`
            };
        } else {
            return {
                passed: true,
                distance: distance.toFixed(2),
                message: `✅ BAŞARILI: Öğrenci ${distance.toFixed(2)}m uzakta (max: ${maxDistance}m) - Yoklama alınmalı`
            };
        }
    } else {
        return {
            passed: null,
            distance: null,
            message: `⚠️  ATLANACAK: Konum bilgisi eksik (öğretmen: ${teacherLat != null && teacherLon != null}, öğrenci: ${studentLat != null && studentLon != null})`
        };
    }
}

console.log("=".repeat(70));
console.log("VALIDATE-QR EDGE FUNCTION - KONUM KONTROLÜ TEST SENARYOLARI");
console.log("=".repeat(70));

// Test Senaryoları
const tests = [
    {
        name: "Senaryo 1: Aynı Konum",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456, lon: 35.123456 },
        expected: "BAŞARILI"
    },
    {
        name: "Senaryo 2: 10 Metre Uzaklık",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456 + 0.00009, lon: 35.123456 }, // ~10m
        expected: "BAŞARILI"
    },
    {
        name: "Senaryo 3: 25 Metre Uzaklık",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456 + 0.000225, lon: 35.123456 }, // ~25m
        expected: "BAŞARILI"
    },
    {
        name: "Senaryo 4: 30 Metre Uzaklık (Tam Sınır)",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456 + 0.00027, lon: 35.123456 }, // ~30m
        expected: "BAŞARILI (sınırda)"
    },
    {
        name: "Senaryo 5: 35 Metre Uzaklık (Sınırı Aşıyor)",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456 + 0.000315, lon: 35.123456 }, // ~35m
        expected: "BAŞARISIZ"
    },
    {
        name: "Senaryo 6: 100 Metre Uzaklık (Çok Uzak)",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: 39.123456 + 0.0009, lon: 35.123456 }, // ~100m
        expected: "BAŞARISIZ"
    },
    {
        name: "Senaryo 7: Öğretmen Konumu Yok",
        teacher: { lat: null, lon: null },
        student: { lat: 39.123456, lon: 35.123456 },
        expected: "ATLANACAK"
    },
    {
        name: "Senaryo 8: Öğrenci Konumu Yok",
        teacher: { lat: 39.123456, lon: 35.123456 },
        student: { lat: null, lon: null },
        expected: "ATLANACAK"
    },
    {
        name: "Senaryo 9: Her İki Konum da Yok",
        teacher: { lat: null, lon: null },
        student: { lat: null, lon: null },
        expected: "ATLANACAK"
    },
    {
        name: "Senaryo 10: Gerçek Koordinatlar (Elazığ - 20m)",
        teacher: { lat: 39.2188, lon: 38.6750 },
        student: { lat: 39.2188 + 0.00018, lon: 38.6750 }, // ~20m
        expected: "BAŞARILI"
    },
    {
        name: "Senaryo 11: Gerçek Koordinatlar (Elazığ - 50m)",
        teacher: { lat: 39.2188, lon: 38.6750 },
        student: { lat: 39.2188 + 0.00045, lon: 38.6750 }, // ~50m
        expected: "BAŞARISIZ"
    }
];

let passedCount = 0;
let failedCount = 0;
let skippedCount = 0;

tests.forEach((test, index) => {
    console.log(`\n${index + 1}. ${test.name}`);
    console.log(`   Öğretmen: ${test.teacher.lat ?? 'null'}, ${test.teacher.lon ?? 'null'}`);
    console.log(`   Öğrenci:  ${test.student.lat ?? 'null'}, ${test.student.lon ?? 'null'}`);
    
    const result = testLocationCheck(
        test.teacher.lat, test.teacher.lon,
        test.student.lat, test.student.lon,
        test.name
    );
    
    console.log(`   ${result.message}`);
    
    if (result.passed === true) {
        passedCount++;
    } else if (result.passed === false) {
        failedCount++;
    } else {
        skippedCount++;
    }
    
    // Beklenen sonuç kontrolü
    if (test.expected === "BAŞARILI" && result.passed === true) {
        console.log(`   ✅ Beklenen sonuçla uyumlu`);
    } else if (test.expected === "BAŞARISIZ" && result.passed === false) {
        console.log(`   ✅ Beklenen sonuçla uyumlu`);
    } else if (test.expected === "ATLANACAK" && result.passed === null) {
        console.log(`   ✅ Beklenen sonuçla uyumlu`);
    } else if (test.expected === "BAŞARILI (sınırda)" && result.passed === true) {
        console.log(`   ✅ Beklenen sonuçla uyumlu (sınırda)`);
    } else {
        console.log(`   ⚠️  Beklenen: ${test.expected}, Alınan: ${result.passed !== null ? (result.passed ? 'BAŞARILI' : 'BAŞARISIZ') : 'ATLANACAK'}`);
    }
});

console.log("\n" + "=".repeat(70));
console.log("TEST ÖZETİ:");
console.log("=".repeat(70));
console.log(`✅ Başarılı: ${passedCount}`);
console.log(`❌ Başarısız: ${failedCount}`);
console.log(`⚠️  Atlanan: ${skippedCount}`);
console.log(`📊 Toplam: ${tests.length}`);
console.log("\n💡 NOT: Bu testler validate-qr edge function'ındaki mesafe hesaplama");
console.log("   mantığını doğrular. Gerçek test için Supabase'de bir QR kod oluşturup");
console.log("   validate-qr endpoint'ini farklı konumlarla çağırmalısın.");
console.log("=".repeat(70));



