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
    private lateinit var binding: ActivityTeacherBinding
    private val apiService = ApiService()
    
    // Dynamic courses loaded from Supabase (fallback includes "Ders Yok")
    private var courses: List<Course> = listOf(
        Course(4, null, "Ders Yok", "", "") // "Ders Yok" has no UUID
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
                        
                        android.util.Log.d("TeacherActivity", "Mapped course: id=$id, courseId=$courseIdLong, name=$name, code=$code")
                        Course(id, courseIdString, name, code, "") // courseId'yi String olarak sakla
                    }
                    
                    android.util.Log.d("TeacherActivity", "Mapped ${mapped.size} courses")
                    
                    if (mapped.isNotEmpty()) {
                        courses = mapped + courses.filter { it.id == 4 }
                        runOnUiThread { 
                            setupCourseSpinner()
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
                    runOnUiThread {
                        Toast.makeText(this@TeacherActivity, "Dersler yüklenirken hata: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val courseAdapter = CustomSpinnerAdapter(
            this,
            courses.map { if (it.code.isNotBlank()) "${it.name} (${it.code})" else it.name }
        )
        binding.spinnerCourse.adapter = courseAdapter
        
        binding.spinnerCourse.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCourse = courses[position]
                updateCourseInfo(selectedCourse)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
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
        if (course.id == 4) { // "Ders Yok" option
            binding.tvStatus.text = "Bu hafta ders yapılmayacak"
            binding.btnGenerateQR.isEnabled = false
        } else {
            binding.tvStatus.text = "${course.name} - ${course.schedule}"
            binding.btnGenerateQR.isEnabled = true
        }
    }
    
    private fun viewAttendanceList() {
        val selectedCourse = courses[binding.spinnerCourse.selectedItemPosition]
        
        // Check if it's "Ders Yok" option
        if (selectedCourse.id == 4) {
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
        if (selectedCourse.id == 4) {
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

                // Mark this week as having QR code created
                qrCreatedWeeks.add(weekKey)

                // Update status with course and week info
                binding.tvStatus.text = "${selectedCourse.name} - ${selectedWeek.name} için QR kod oluşturuldu!"

                val qrBitmap = generateQRBitmap(qrJson)
                binding.ivQRCode.setImageBitmap(qrBitmap)
                binding.tvStatus.text = "${selectedCourse.name} - ${selectedWeek.name} için QR kod oluşturuldu! ($duration dakika geçerli)"
                binding.btnStopAttendance.isEnabled = true

                // Start countdown
                startCountdown(duration * 60)

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
        var remainingSeconds = seconds
        val timer = Timer()
        
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val minutes = remainingSeconds / 60
                    val secs = remainingSeconds % 60
                    binding.tvCountdown.text = String.format("%02d:%02d", minutes, secs)
                    
                    if (remainingSeconds <= 0) {
                        timer.cancel()
                        binding.tvStatus.text = "QR Code expired"
                        binding.btnStopAttendance.isEnabled = false
                        binding.ivQRCode.setImageDrawable(null)
                    }
                    remainingSeconds--
                }
            }
        }, 0, 1000)
    }

    private fun stopAttendance() {
        binding.tvStatus.text = "Attendance stopped"
        binding.tvCountdown.text = "00:00"
        binding.btnStopAttendance.isEnabled = false
        binding.ivQRCode.setImageDrawable(null)
    }
}
