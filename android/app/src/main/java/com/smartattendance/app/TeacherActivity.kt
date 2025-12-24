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
        Course(id = 4, name = "Ders Yok", code = "", schedule = "", weeklyHours = 2)
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
    
    // Track selected sessions for QR code generation
    private val selectedSessions = mutableListOf<Int>()
    
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
            lifecycleScope.launch {
                val assigned = apiService.getAssignedCoursesForTeacher(email)
                val mapped: List<Course> = (assigned ?: emptyList()).mapNotNull { row ->
                    val id = row.courseId?.toInt() ?: return@mapNotNull null
                    val name = row.courseName ?: return@mapNotNull null
                    val code = row.courseCode ?: ""
                    val weeklyHours = row.weeklyHours ?: 2 // API'den gelen değer veya default 2
                    Course(id = id, name = name, code = code, schedule = "", weeklyHours = weeklyHours)
                }
                if (mapped.isNotEmpty()) {
                    courses = mapped + courses.filter { it.id == 4 }
                    runOnUiThread { setupCourseSpinner() }
                }
            }
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
        
        binding.spinnerWeek.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCourse = courses[binding.spinnerCourse.selectedItemPosition]
                if (selectedCourse.id != 4) {
                    loadWeeklySessions(selectedCourse.id.toLong(), weeks[position].id)
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun updateCourseInfo(course: Course) {
        if (course.id == 4) { // "Ders Yok" option
            binding.tvStatus.text = "Bu hafta ders yapılmayacak"
            binding.btnGenerateQR.isEnabled = false
            binding.llSessionsContainer.visibility = android.view.View.GONE
            binding.tvSessionsInfo.visibility = android.view.View.GONE
        } else {
            binding.tvStatus.text = "${course.name} - ${course.schedule}"
            binding.btnGenerateQR.isEnabled = false // Oturum seçilene kadar devre dışı
            binding.llSessionsContainer.visibility = android.view.View.VISIBLE
            binding.tvSessionsInfo.visibility = android.view.View.VISIBLE
            // Seçili hafta için oturumları yükle
            val selectedWeekPosition = binding.spinnerWeek.selectedItemPosition
            if (selectedWeekPosition >= 0) {
                loadWeeklySessions(course.id.toLong(), weeks[selectedWeekPosition].id)
            }
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
    
    private fun loadWeeklySessions(courseId: Long, weekNumber: Int) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        selectedSessions.clear()
        binding.llSessionsContainer.removeAllViews()
        
        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    apiService.getWeeklySessions(courseId, weekNumber)
                }
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (response == null || response.availableSessions.isEmpty()) {
                        binding.tvSessionsInfo.text = "Bu hafta için oturum bulunamadı"
                        binding.btnGenerateQR.isEnabled = false
                        return@runOnUiThread
                    }
                    
                    // Oturum bilgilerini göster
                    val completedText = if (response.completedSessions > 0) {
                        " (${response.completedSessions}/${response.totalSessions} tamamlandı)"
                    } else {
                        ""
                    }
                    binding.tvSessionsInfo.text = "Seçili hafta için ${response.totalSessions} oturum var$completedText. Lütfen QR kod için oturum seçin:"
                    
                    // Checkbox'ları oluştur
                    response.availableSessions.forEach { session ->
                        val checkBox = android.widget.CheckBox(this@TeacherActivity).apply {
                            text = "${session.sessionNumber}. Oturum"
                            if (session.isCompleted) {
                                text = "${session.sessionNumber}. Oturum (Tamamlandı)"
                                isEnabled = false
                                setTextColor(android.graphics.Color.GRAY)
                            } else {
                                setTextColor(ContextCompat.getColor(this@TeacherActivity, android.R.color.black))
                            }
                            tag = session.sessionNumber
                            setOnCheckedChangeListener { _, isChecked ->
                                val sessionNum = tag as Int
                                if (isChecked) {
                                    if (!selectedSessions.contains(sessionNum)) {
                                        selectedSessions.add(sessionNum)
                                    }
                                } else {
                                    selectedSessions.remove(sessionNum)
                                }
                                updateGenerateQRButtonState()
                            }
                        }
                        binding.llSessionsContainer.addView(checkBox)
                    }
                    
                    updateGenerateQRButtonState()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.tvSessionsInfo.text = "Oturumlar yüklenirken hata: ${e.message}"
                    Toast.makeText(this@TeacherActivity, "Oturumlar yüklenirken hata: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnGenerateQR.isEnabled = false
                }
            }
        }
    }
    
    private fun updateGenerateQRButtonState() {
        binding.btnGenerateQR.isEnabled = selectedSessions.isNotEmpty()
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
        
        // Check if sessions are selected
        if (selectedSessions.isEmpty()) {
            Toast.makeText(this, "Lütfen en az bir ders oturumu seçin", Toast.LENGTH_SHORT).show()
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
                val response = withContext(Dispatchers.IO) {
                    apiService.createQRCode(
                        courseId = selectedCourse.id.toLong(),
                        weekNumber = selectedWeek.id,
                        expireAfterMinutes = duration,
                        sessionNumbers = selectedSessions
                    )
                }

                if (response == null) {
                    Toast.makeText(this@TeacherActivity, "QR kod oluşturulamadı", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val qrData = response.qr

                // Create JSON string for QR code matching server schema
                val teacherLatStr = if (qrData.teacherLatitude != null) ""","teacher_latitude": ${qrData.teacherLatitude}""" else ""
                val teacherLonStr = if (qrData.teacherLongitude != null) ""","teacher_longitude": ${qrData.teacherLongitude}""" else ""
                val qrJson = """
                    {
                        "course_id": ${qrData.courseId},
                        "week_number": ${qrData.weekNumber},
                        "created_at": "${qrData.createdAt}",
                        "expire_after": ${qrData.expireAfter}$teacherLatStr$teacherLonStr
                    }
                """.trimIndent()

                // Mark this week as having QR code created
                qrCreatedWeeks.add(weekKey)

                // Update status with course and week info
                val sessionsText = if (selectedSessions.size == 1) {
                    "${selectedSessions.first()}. oturum"
                } else {
                    "${selectedSessions.sorted().joinToString(", ")}. oturumlar"
                }
                binding.tvStatus.text = "${selectedCourse.name} - ${selectedWeek.name} için QR kod oluşturuldu! ($sessionsText, $duration dakika geçerli)"

                val qrBitmap = generateQRBitmap(qrJson)
                binding.ivQRCode.setImageBitmap(qrBitmap)
                binding.btnStopAttendance.isEnabled = true

                // Start countdown
                startCountdown(duration * 60)
                
                // Reload sessions to update UI (completed sessions will be disabled)
                loadWeeklySessions(selectedCourse.id.toLong(), selectedWeek.id)

            } catch (e: Exception) {
                Toast.makeText(this@TeacherActivity, "QR oluşturma hatası: ${e.message}", Toast.LENGTH_LONG).show()
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
