package com.smartattendance.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.smartattendance.app.databinding.ActivityTeacherBinding
import com.smartattendance.app.QRData
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TeacherActivity : AppCompatActivity() {
    private val NO_COURSE_ID = -1
    private val prefs by lazy { getSharedPreferences("teacher_qr", MODE_PRIVATE) }
    private var countdownTimer: Timer? = null
    private lateinit var binding: ActivityTeacherBinding
    private val apiService = ApiService()
    
    // Dynamic courses loaded from Supabase (fallback includes "Ders Yok")
    private var courses: List<Course> = listOf(
        Course(NO_COURSE_ID, null, "Ders Yok", "", "") // "Ders Yok" has no UUID
    )
    
    private val weeks = listOf(
        Week(1, "1. Hafta"),
        Week(2, "2. Hafta"),
        Week(3, "3. Hafta"),
        Week(4, "4. Hafta"),
        Week(5, "5. Hafta"),
        Week(6, "6. Hafta"),
        Week(7, "7. Hafta"),
        Week(8, "8. Hafta"),
        Week(9, "9. Hafta"),
        Week(10, "10. Hafta"),
        Week(11, "11. Hafta"),
        Week(12, "12. Hafta"),
        Week(13, "13. Hafta"),
        Week(14, "14. Hafta")
    )
    
    // Track which weeks have QR codes created
    private val qrCreatedWeeks = mutableSetOf<Pair<Int, Int>>() // (courseId, weekId)
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            generateQRCode()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get user info from login
        val userType = intent.getStringExtra("user_type") ?: "teacher"
        val email = intent.getStringExtra("email") ?: ""

        setupUI()
        restoreCreatedWeeks()
        restoreActiveQRIfAny()

        // Load assigned courses for this teacher by email (if available)
        if (email.isNotBlank()) {
            android.util.Log.d("TeacherActivity", "Loading courses for teacher email: $email")
            lifecycleScope.launch {
                try {
                    val assigned = apiService.getAssignedCoursesForTeacher(email)
                    android.util.Log.d("TeacherActivity", "Received ${assigned?.size ?: 0} assigned courses")
                    
                    val mapped: List<Course> = (assigned ?: emptyList()).mapIndexedNotNull { index, row ->
                        android.util.Log.d("TeacherActivity", "Processing course $index: id=${row.courseId}, name=${row.courseName}")
                        
                        // course_id BIGINT (Long) olarak geliyor, hash'e çevirerek Int ID oluştur (UI için)
                        val courseIdLong = row.courseId
                        val id = if (courseIdLong != null && courseIdLong > 0) {
                            // Long'u Int'e çevir (pozitif sayı garantisi)
                            kotlin.math.abs(courseIdLong.toInt())
                        } else {
                            android.util.Log.w("TeacherActivity", "Course $index has null/invalid course_id")
                            return@mapIndexedNotNull null
                        }
                        
                        val name = row.courseName ?: return@mapIndexedNotNull null
                        val code = row.courseCode ?: ""
                        
                        // courseIdLong'u String'e çevirip uuid field'ında sakla (QR oluştururken kullanmak için)
                        val courseIdString = courseIdLong.toString()
                        
                        android.util.Log.d("TeacherActivity", "Mapped course: id=$id, courseId=$courseIdLong, name=$name, code=$code, uuid=$courseIdString")
                        Course(id, courseIdString, name, code, "") // courseId'yi String olarak sakla
                    }
                    
                    android.util.Log.d("TeacherActivity", "Mapped ${mapped.size} courses")
                    
                    if (mapped.isNotEmpty()) {
                        courses = mapped + courses.filter { it.id == NO_COURSE_ID }
                        runOnUiThread { 
                            updateCourseSpinnerAdapter()
                            android.util.Log.d("TeacherActivity", "Course spinner updated with ${courses.size} courses")
                        }
                    } else {
                        android.util.Log.w("TeacherActivity", "No courses mapped, keeping default")
                        runOnUiThread {
                            Toast.makeText(this@TeacherActivity, "Bu öğretmene atanmış ders bulunamadı", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TeacherActivity", "Error loading courses: ${e.message}", e)
                    val errorMessage = when {
                        e.message?.contains("Unable to resolve host") == true || 
                        e.message?.contains("No address associated") == true -> 
                            "İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edin."
                        e.message?.contains("timeout") == true || 
                        e.message?.contains("SocketTimeoutException") == true -> 
                            "Bağlantı zaman aşımına uğradı. Lütfen tekrar deneyin."
                        else -> "Dersler yüklenirken hata: ${e.message}"
                    }
                    runOnUiThread {
                        Toast.makeText(this@TeacherActivity, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            android.util.Log.w("TeacherActivity", "No email provided, using default courses")
        }
    }

    private fun setupUI() {
        // Setup spinners
        setupCourseSpinner()
        setupWeekSpinner()
        
        // QR Code generation
        binding.btnGenerateQR.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                generateQRCode()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        binding.btnStopAttendance.setOnClickListener {
            stopAttendance()
        }
        
        // Attendance control
        binding.btnViewAttendance.setOnClickListener {
            val email = intent.getStringExtra("email") ?: ""
            val go = Intent(this, AttendanceListActivity::class.java)
            if (email.isNotBlank()) {
                go.putExtra("email", email)
            }
            startActivity(go)
        }
        
        // Navigation
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun setupCourseSpinner() {
        updateCourseSpinnerAdapter()
        
        binding.spinnerCourse.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                android.util.Log.d("TeacherActivity", "Spinner item selected: position=$position, total=${courses.size}")
                if (position < courses.size) {
                    val selectedCourse = courses[position]
                    android.util.Log.d("TeacherActivity", "Selected course: ${selectedCourse.name}, id=${selectedCourse.id}, uuid=${selectedCourse.uuid}")
                    updateCourseInfo(selectedCourse)
                } else {
                    android.util.Log.e("TeacherActivity", "Invalid position: $position >= ${courses.size}")
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                android.util.Log.d("TeacherActivity", "Nothing selected in spinner")
            }
        }
    }
    
    private fun updateCourseSpinnerAdapter() {
        android.util.Log.d("TeacherActivity", "updateCourseSpinnerAdapter: ${courses.size} courses")
        val courseAdapter = CustomSpinnerAdapter(
            this,
            courses.map { if (it.code.isNotBlank()) "${it.name} (${it.code})" else it.name }
        )
        binding.spinnerCourse.adapter = courseAdapter
    }
    
    private fun setupWeekSpinner() {
        val weekAdapter = CustomSpinnerAdapter(
            this,
            weeks.map { it.name }
            // Default color (#424242) kullanılacak - daha açık gri
        )
        binding.spinnerWeek.adapter = weekAdapter
    }
    
    private fun updateCourseInfo(course: Course) {
        // QR kod aktifse status'u değiştirme (QR kodun kendi ders bilgisi gösterilmeli)
        val qrIsActive = binding.btnStopAttendance.isEnabled
        
        if (course.id == NO_COURSE_ID) { // "Ders Yok" option
            if (!qrIsActive) {
                binding.tvStatus.text = "Bu hafta ders yapılmayacak"
            }
            binding.btnGenerateQR.isEnabled = false
        } else {
            if (!qrIsActive) {
                binding.tvStatus.text = "${course.name} - ${course.schedule}"
            }
            binding.btnGenerateQR.isEnabled = true
            // Sunucudan bu ders için oluşturulmuş QR haftalarını çek
            android.util.Log.d("TeacherActivity", "updateCourseInfo: ${course.name}, uuid=${course.uuid}, id=${course.id}")
            refreshWeeksFromServer(course)
        }
    }
    
    private fun viewAttendanceList() {
        val selectedCourse = courses[binding.spinnerCourse.selectedItemPosition]
        
        // Check if it's "Ders Yok" option
        if (selectedCourse.id == NO_COURSE_ID) {
            Toast.makeText(this, "Bu ders için yoklama alınmıyor", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Tüm haftaları göster (QR kod oluşturulmuş olmasa bile)
        showAllWeeksDialog(selectedCourse)
    }
    
    private fun showAllWeeksDialog(course: Course) {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("${course.name} - Hafta Seçimi")
        
        val weekNames = weeks.map { "Hafta ${it.id} - ${it.name}" }.toTypedArray()
        
        dialog.setItems(weekNames) { _, which ->
            val selectedWeek = weeks[which]
            showAttendanceForWeek(course, selectedWeek)
        }
        
        dialog.setNegativeButton("Geri") { dialog, _ ->
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun getAvailableWeeksForCourse(courseId: Int): List<Week> {
        return weeks.filter { week ->
            val weekKey = Pair(courseId, week.id)
            qrCreatedWeeks.contains(weekKey)
        }
    }
    
    
    private fun showAttendanceForWeek(course: Course, week: Week) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val attendanceData = apiService.getAttendanceForWeek(course.id, week.id)
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (attendanceData.isNullOrEmpty()) {
                        Toast.makeText(this@TeacherActivity, "Bu hafta için yoklama verisi bulunamadı", Toast.LENGTH_SHORT).show()
                    } else {
                        showAttendanceDialog(course.name, "Hafta ${week.id}", attendanceData)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@TeacherActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    
    private fun showAttendanceDialog(courseName: String, weekName: String, attendanceData: List<AttendanceRecord>) {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("$courseName - $weekName")
        dialog.setMessage("Yoklama Listesi:\n\n" + 
            attendanceData.joinToString("\n") { 
                "${it.profiles?.fullName ?: "Bilinmeyen"} - ${it.markedAt} - ${it.method}" 
            })
        dialog.setPositiveButton("Tamam") { _, _ -> }
        dialog.setNegativeButton("Geri") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }
    
    private fun logout() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
    

    private fun generateQRCode() {
        val selectedCourse = courses[binding.spinnerCourse.selectedItemPosition]
        val selectedWeek = weeks[binding.spinnerWeek.selectedItemPosition]
        
        // Check if QR code already exists for this course and week
        val weekKey = Pair(selectedCourse.id, selectedWeek.id)
        if (qrCreatedWeeks.contains(weekKey)) {
            Toast.makeText(this, "Bu hafta için QR kod zaten oluşturulmuş! Yoklama listesini görüntüleyin.", Toast.LENGTH_LONG).show()
            return
        }
        
        // Check if it's "Ders Yok" option
        if (selectedCourse.id == NO_COURSE_ID) {
            Toast.makeText(this, "Bu hafta ders yapılmayacak", Toast.LENGTH_SHORT).show()
            return
        }
        
        val duration = when {
            binding.rb5min.isChecked -> 5
            binding.rb10min.isChecked -> 10
            binding.rb15min.isChecked -> 15
            binding.rb30min.isChecked -> 30
            binding.rb60min.isChecked -> 60
            else -> 60
        }

        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.btnGenerateQR.isEnabled = false

        lifecycleScope.launch {
            try {
                // Create QR on server (Edge Function) so student validation can find it in DB
                // courseId is stored as String in Course.uuid field, convert to Long
                val courseIdToSend = if (!selectedCourse.uuid.isNullOrBlank()) {
                    try {
                        selectedCourse.uuid.toLong()
                    } catch (e: NumberFormatException) {
                        android.util.Log.e("TeacherActivity", "Invalid courseId format: ${selectedCourse.uuid}")
                        Toast.makeText(this@TeacherActivity, "Ders ID geçersiz. Lütfen geçerli bir ders seçin.", Toast.LENGTH_LONG).show()
                        binding.btnGenerateQR.isEnabled = true
                        return@launch
                    }
                } else {
                    // This should not happen for valid courses, but handle gracefully
                    android.util.Log.e("TeacherActivity", "Missing courseId for course: ${selectedCourse.name}, id=${selectedCourse.id}")
                    Toast.makeText(this@TeacherActivity, "Ders ID bulunamadı. Lütfen geçerli bir ders seçin.", Toast.LENGTH_LONG).show()
                    binding.btnGenerateQR.isEnabled = true
                    return@launch
                }
                
                android.util.Log.d("TeacherActivity", "Creating QR with courseId (BIGINT): $courseIdToSend (course=${selectedCourse.name})")
                
                val response = withContext(Dispatchers.IO) {
                    apiService.createQRCode(
                        courseId = courseIdToSend,
                        weekNumber = selectedWeek.id,
                        expireAfterMinutes = duration
                    )
                }

                if (response == null) {
                    Toast.makeText(this@TeacherActivity, "QR kod oluşturulamadı", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val qrData = response.qr

                // Create JSON string for QR code matching server schema
                val qrJson = """
                    {
                        "course_id": ${qrData.courseId},
                        "week_number": ${qrData.weekNumber},
                        "created_at": "${qrData.createdAt}",
                        "expire_after": ${qrData.expireAfter}
                    }
                """.trimIndent()

                // Mark this week as having QR code created (persisted)
                qrCreatedWeeks.add(weekKey)
                persistCreatedWeeks()

                // Update status with course and week info
                binding.tvStatus.text = "${selectedCourse.name} - ${selectedWeek.name} için QR kod oluşturuldu!"

                val expiresAtEpoch = runCatching {
                    Instant.parse(qrData.createdAt).epochSecond + qrData.expireAfter * 60
                }.getOrElse {
                    android.util.Log.w("TeacherActivity", "Unable to parse createdAt, using now for expiry")
                    Instant.now().epochSecond + qrData.expireAfter * 60
                }

                persistActiveQR(
                    qrJson = qrJson,
                    expiresAt = expiresAtEpoch,
                    courseName = selectedCourse.name,
                    weekName = selectedWeek.name
                )

                showActiveQR(qrJson, expiresAtEpoch, selectedCourse.name, selectedWeek.name)

            } catch (e: Exception) {
                android.util.Log.e("TeacherActivity", "QR creation error: ${e.javaClass.simpleName} - ${e.message}", e)
                val errorMessage = when {
                    e.message?.contains("Failed to connect") == true -> "Sunucuya bağlanılamadı. İnternet bağlantınızı kontrol edin."
                    e.message?.contains("timeout") == true -> "İstek zaman aşımına uğradı. Lütfen tekrar deneyin."
                    e.message?.contains("Unknown host") == true -> "Sunucu bulunamadı. İnternet bağlantınızı kontrol edin."
                    else -> "QR kod oluşturma hatası: ${e.message ?: "Bilinmeyen hata"}"
                }
                Toast.makeText(this@TeacherActivity, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = android.view.View.GONE
                binding.btnGenerateQR.isEnabled = true
            }
        }
    }
    
    private fun getSelectedCourseId(): Int {
        val selectedPosition = binding.spinnerCourse.selectedItemPosition
        return courses[selectedPosition].id
    }
    
    private fun getSelectedWeekId(): Int {
        val selectedPosition = binding.spinnerWeek.selectedItemPosition
        return weeks[selectedPosition].id
    }

    private fun generateQRBitmap(data: String): android.graphics.Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun startCountdown(seconds: Int) {
        countdownTimer?.cancel()
        var remainingSeconds = seconds
        countdownTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    runOnUiThread {
                        val minutes = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        binding.tvCountdown.text = String.format("%02d:%02d", minutes, secs)

                        if (remainingSeconds <= 0) {
                            cancel()
                            clearActiveQR()
                            binding.tvStatus.text = "QR süresi doldu"
                            binding.btnStopAttendance.isEnabled = false
                            binding.ivQRCode.setImageDrawable(null)
                        }
                        remainingSeconds--
                    }
                }
            }, 0, 1000)
        }
    }

    private fun stopAttendance() {
        countdownTimer?.cancel()
        clearActiveQR()
        binding.tvStatus.text = "Yoklama durduruldu"
        binding.tvCountdown.text = "00:00"
        binding.btnStopAttendance.isEnabled = false
        binding.ivQRCode.setImageDrawable(null)
    }

    private fun persistActiveQR(qrJson: String, expiresAt: Long, courseName: String, weekName: String) {
        prefs.edit()
            .putString("qr_json", qrJson)
            .putLong("qr_expires_at", expiresAt)
            .putString("qr_course_name", courseName)
            .putString("qr_week_name", weekName)
            .apply()
    }

    private fun clearActiveQR() {
        prefs.edit()
            .remove("qr_json")
            .remove("qr_expires_at")
            .remove("qr_course_name")
            .remove("qr_week_name")
            .apply()
    }

    private fun persistCreatedWeeks() {
        // store as comma-separated "courseId-weekId"
        val serialized = qrCreatedWeeks.joinToString(",") { "${it.first}-${it.second}" }
        prefs.edit().putString("qr_created_weeks", serialized).apply()
    }

    private fun restoreCreatedWeeks() {
        val serialized = prefs.getString("qr_created_weeks", null) ?: return
        if (serialized.isBlank()) return
        serialized.split(",").forEach { entry ->
            val parts = entry.split("-")
            if (parts.size == 2) {
                val cId = parts[0].toIntOrNull()
                val wId = parts[1].toIntOrNull()
                if (cId != null && wId != null) {
                    qrCreatedWeeks.add(Pair(cId, wId))
                }
            }
        }
    }

    private fun restoreActiveQRIfAny() {
        val qrJson = prefs.getString("qr_json", null) ?: return
        val expiresAt = prefs.getLong("qr_expires_at", 0L)
        val courseName = prefs.getString("qr_course_name", "") ?: ""
        val weekName = prefs.getString("qr_week_name", "") ?: ""

        if (expiresAt <= 0L) {
            clearActiveQR()
            return
        }

        val now = Instant.now().epochSecond
        val remaining = (expiresAt - now).toInt()
        if (remaining <= 0) {
            clearActiveQR()
            return
        }

        showActiveQR(qrJson, expiresAt, courseName, weekName)
    }

    private fun showActiveQR(qrJson: String, expiresAt: Long, courseName: String, weekName: String) {
        val qrBitmap = generateQRBitmap(qrJson)
        binding.ivQRCode.setImageBitmap(qrBitmap)
        val now = Instant.now().epochSecond
        val remainingSeconds = (expiresAt - now).toInt().coerceAtLeast(0)
        binding.tvStatus.text = "$courseName - $weekName için QR kod aktif"
        binding.btnStopAttendance.isEnabled = true
        startCountdown(remainingSeconds)
    }

    private fun refreshWeeksFromServer(course: Course) {
        android.util.Log.d("TeacherActivity", "=== refreshWeeksFromServer START ===")
        android.util.Log.d("TeacherActivity", "Course: name=${course.name}, id=${course.id}, uuid=${course.uuid}, uuid.isNullOrBlank=${course.uuid.isNullOrBlank()}")
        
        val courseIdLong = course.uuid?.toLongOrNull()
        if (courseIdLong == null) {
            android.util.Log.e("TeacherActivity", "refreshWeeksFromServer FAILED: courseIdLong is null for ${course.name}")
            android.util.Log.e("TeacherActivity", "  - course.uuid = ${course.uuid}")
            android.util.Log.e("TeacherActivity", "  - course.uuid?.toLongOrNull() = null")
            runOnUiThread {
                Toast.makeText(this@TeacherActivity, "Ders ID bulunamadı: ${course.name}", Toast.LENGTH_SHORT).show()
            }
            return
        }

        android.util.Log.d("TeacherActivity", "refreshWeeksFromServer: Fetching weeks for courseId=$courseIdLong (${course.name})")
        android.util.Log.d("TeacherActivity", "🔍 DEBUG: Querying qr_codes WHERE course_id=$courseIdLong AND is_active=true")
        lifecycleScope.launch {
            try {
                val weeks = apiService.getWeeksWithQR(courseIdLong)
                android.util.Log.d("TeacherActivity", "refreshWeeksFromServer: API returned ${weeks?.size ?: 0} weeks for ${course.name} (courseId=$courseIdLong)")
                if (weeks != null && weeks.isNotEmpty()) {
                    // Bu derse ait önceki kayıtları temizle, gelen haftaları ekle
                    val removed = qrCreatedWeeks.removeAll { it.first == course.id }
                    android.util.Log.d("TeacherActivity", "Removed $removed old entries for course.id=${course.id}")
                    weeks.forEach { week ->
                        qrCreatedWeeks.add(Pair(course.id, week.week_number))
                        android.util.Log.d("TeacherActivity", "Added week: course.id=${course.id}, week.week_number=${week.week_number}")
                    }
                    persistCreatedWeeks()
                    android.util.Log.d("TeacherActivity", "=== refreshWeeksFromServer SUCCESS: ${weeks.size} weeks for ${course.name} ===")
                } else {
                    // Boş liste normal olabilir (henüz QR oluşturulmamış)
                    qrCreatedWeeks.removeAll { it.first == course.id }
                    persistCreatedWeeks()
                    android.util.Log.d("TeacherActivity", "=== refreshWeeksFromServer: No weeks found for ${course.name} ===")
                }
            } catch (e: Exception) {
                android.util.Log.e("TeacherActivity", "=== refreshWeeksFromServer ERROR for ${course.name}: ${e.message} ===", e)
            }
        }
    }
}
