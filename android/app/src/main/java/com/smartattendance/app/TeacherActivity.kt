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
    
    // Dummy data
    private val courses = listOf(
        Course(1, "Veri Yapıları ve Algoritmalar", "CS201", "Pazartesi 09:00-11:00"),
        Course(2, "Yazılım Mühendisliği", "CS301", "Salı 13:00-15:00"),
        Course(3, "Veritabanı Sistemleri", "CS401", "Çarşamba 10:00-12:00"),
        Course(4, "Ders Yok", "", "")
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
        val email = intent.getStringExtra("email") ?: "admin"

        setupUI()
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
            viewAttendanceList()
        }
        
        binding.btnExportAttendance.setOnClickListener {
            exportAttendanceReport()
        }
        
        // Navigation
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    
    private fun setupCourseSpinner() {
        val courseAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            courses.map { "${it.name} (${it.code})" }
        )
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
        val weekAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            weeks.map { it.name }
        )
        weekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
        
        // Get weeks that have QR codes created for this course
        val availableWeeks = getAvailableWeeksForCourse(selectedCourse.id)
        
        if (availableWeeks.isEmpty()) {
            Toast.makeText(this, "Bu ders için henüz QR kod oluşturulmamış", Toast.LENGTH_LONG).show()
            return
        }
        
        // Show week selection dialog
        showWeekSelectionDialog(selectedCourse, availableWeeks)
    }
    
    private fun getAvailableWeeksForCourse(courseId: Int): List<Week> {
        return weeks.filter { week ->
            val weekKey = Pair(courseId, week.id)
            qrCreatedWeeks.contains(weekKey)
        }
    }
    
    private fun showWeekSelectionDialog(course: Course, availableWeeks: List<Week>) {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("${course.name} - Hafta Seçimi")
        
        val weekNames = availableWeeks.map { it.name }.toTypedArray()
        
        dialog.setItems(weekNames) { _, which ->
            val selectedWeek = availableWeeks[which]
            showAttendanceForWeek(course, selectedWeek)
        }
        
        dialog.setNegativeButton("Geri") { dialog, _ ->
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun showAttendanceForWeek(course: Course, week: Week) {
        val attendanceData = getAttendanceData(course.id, week.id)
        
        if (attendanceData.isEmpty()) {
            Toast.makeText(this, "Bu hafta için yoklama verisi bulunamadı", Toast.LENGTH_SHORT).show()
        } else {
            showAttendanceDialog(course.name, week.name, attendanceData)
        }
    }
    
    private fun getAttendanceData(courseId: Int, weekId: Int): List<AttendanceRecord> {
        // Dummy attendance data - in real system, this would come from database
        return when {
            courseId == 1 && weekId == 1 -> listOf(
                AttendanceRecord("Ahmet Yılmaz", "2024-01-15 09:00", "Present"),
                AttendanceRecord("Ayşe Demir", "2024-01-15 09:05", "Present"),
                AttendanceRecord("Mehmet Kaya", "2024-01-15 09:10", "Present"),
                AttendanceRecord("Fatma Öz", "2024-01-15 09:15", "Present")
            )
            courseId == 1 && weekId == 2 -> listOf(
                AttendanceRecord("Ahmet Yılmaz", "2024-01-22 09:00", "Present"),
                AttendanceRecord("Ayşe Demir", "2024-01-22 09:05", "Present"),
                AttendanceRecord("Mehmet Kaya", "2024-01-22 09:10", "Present")
            )
            courseId == 2 && weekId == 1 -> listOf(
                AttendanceRecord("Ali Veli", "2024-01-16 13:00", "Present"),
                AttendanceRecord("Zeynep Ak", "2024-01-16 13:05", "Present"),
                AttendanceRecord("Can Özkan", "2024-01-16 13:10", "Present")
            )
            courseId == 2 && weekId == 2 -> listOf(
                AttendanceRecord("Ali Veli", "2024-01-23 13:00", "Present"),
                AttendanceRecord("Zeynep Ak", "2024-01-23 13:05", "Present")
            )
            courseId == 3 && weekId == 1 -> listOf(
                AttendanceRecord("Emre Şen", "2024-01-17 10:00", "Present"),
                AttendanceRecord("Selin Yıldız", "2024-01-17 10:05", "Present"),
                AttendanceRecord("Burak Çelik", "2024-01-17 10:10", "Present")
            )
            else -> emptyList()
        }
    }
    
    private fun showAttendanceDialog(courseName: String, weekName: String, attendanceData: List<AttendanceRecord>) {
        val dialog = android.app.AlertDialog.Builder(this)
        dialog.setTitle("$courseName - $weekName")
        dialog.setMessage("Yoklama Listesi:\n\n" + 
            attendanceData.joinToString("\n") { 
                "${it.studentName} - ${it.time} - ${it.status}" 
            })
        dialog.setPositiveButton("Tamam") { _, _ -> }
        dialog.setNegativeButton("Geri") { dialog, _ ->
            dialog.dismiss()
        }
        dialog.show()
    }
    
    private fun exportAttendanceReport() {
        Toast.makeText(this, "Yoklama raporu dışa aktarılıyor...", Toast.LENGTH_SHORT).show()
        // TODO: Implement export functionality
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
                val response = withContext(Dispatchers.IO) {
                    apiService.createQRCode(
                        courseId = selectedCourse.id,
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
