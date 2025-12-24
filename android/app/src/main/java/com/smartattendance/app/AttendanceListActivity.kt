package com.smartattendance.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartattendance.app.databinding.ActivityAttendanceListBinding
import kotlinx.coroutines.launch

class AttendanceListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAttendanceListBinding
    private lateinit var supabaseService: SupabaseService
    private val apiService = ApiService()
    private lateinit var weekAdapter: WeekAdapter
    private lateinit var attendanceAdapter: AttendanceAdapter
    
    private var courses: List<Course> = listOf(
        Course(4, null, "Ders Yok", "N/A", "Bu hafta ders yapılmayacak") // "Ders Yok" has no UUID
    )
    
    private var selectedCourse: Course? = null
    private var weeksWithQR: List<WeekWithQR> = emptyList()
    private var currentAttendance: List<StudentRecord> = emptyList()
    private var totalStudents: Int = 0
    private var attendedStudents: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAttendanceListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        supabaseService = SupabaseService()
        setupUI()

        // Load assigned courses for this teacher by email (if provided)
        val email = intent.getStringExtra("email") ?: ""
        if (email.isNotBlank()) {
            lifecycleScope.launch {
                val assigned = apiService.getAssignedCoursesForTeacher(email)
                val mapped: List<Course> = (assigned ?: emptyList()).mapIndexedNotNull { index, row ->
                    android.util.Log.d("AttendanceListActivity", "Processing course $index: id=${row.courseId}, name=${row.courseName}")
                    
                    // course_id BIGINT (Long) olarak geliyor, Int ID oluştur (UI için)
                    val courseIdLong = row.courseId
                    val id = if (courseIdLong != null && courseIdLong > 0) {
                        // Long'u Int'e çevir (pozitif sayı garantisi)
                        kotlin.math.abs(courseIdLong.toInt())
                    } else {
                        android.util.Log.w("AttendanceListActivity", "Course $index has null/invalid course_id")
                        return@mapIndexedNotNull null
                    }
                    
                    val name = row.courseName ?: return@mapIndexedNotNull null
                    val code = row.courseCode ?: ""
                    val weeklyHours = row.weeklyHours ?: 2 // API'den gelen değer veya default 2
                    
                    // courseIdLong'u String'e çevirip uuid field'ında sakla (API çağrıları için)
                    val courseIdString = courseIdLong.toString()
                    
                    android.util.Log.d("AttendanceListActivity", "Mapped course: id=$id, courseId=$courseIdLong, name=$name, code=$code, weeklyHours=$weeklyHours")
                    Course(id, courseIdString, name, code, "", weeklyHours) // courseId'yi String olarak sakla
                }
                if (mapped.isNotEmpty()) {
                    courses = mapped
                    runOnUiThread { setupCourseSpinner() }
                }
            }
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
                selectedCourse = courses[position]
                if (selectedCourse != null) {
                    loadWeeksForCourse(selectedCourse!!)
                } else {
                    weeksWithQR = emptyList()
                    weekAdapter.updateWeeks(emptyList())
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun setupUI() {
        // Setup course spinner
        setupCourseSpinner()
        
        // Setup week RecyclerView
        weekAdapter = WeekAdapter { week ->
            loadAttendanceForWeek(week)
        }
        binding.recyclerViewWeeks.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewWeeks.adapter = weekAdapter
        
        // Setup attendance RecyclerView
        attendanceAdapter = AttendanceAdapter()
        binding.recyclerViewAttendance.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewAttendance.adapter = attendanceAdapter
        
        // Navigation
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Setup export button
        binding.btnExportReport.setOnClickListener {
            exportAttendanceReport()
        }
        
        // Initially hide attendance section
        binding.layoutAttendance.visibility = android.view.View.GONE
    }
    
    private fun loadWeeksForCourse(course: Course) {
        android.util.Log.d("AttendanceListActivity", "Loading weeks for course: ${course.id} - ${course.name}")
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.layoutAttendance.visibility = android.view.View.GONE
        
        lifecycleScope.launch {
            try {
                // Convert course.uuid (String) to Long for API call
                val courseIdLong = if (!course.uuid.isNullOrBlank()) {
                    try {
                        course.uuid.toLong()
                    } catch (e: NumberFormatException) {
                        android.util.Log.e("AttendanceListActivity", "Invalid courseId format: ${course.uuid}")
                        runOnUiThread {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(this@AttendanceListActivity, "Ders ID geçersiz", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                } else {
                    android.util.Log.e("AttendanceListActivity", "Missing courseId for course: ${course.name}")
                    runOnUiThread {
                        binding.progressBar.visibility = android.view.View.GONE
                        Toast.makeText(this@AttendanceListActivity, "Ders ID bulunamadı", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                
                val weeks = supabaseService.getWeeksWithQR(courseIdLong)
                android.util.Log.d("AttendanceListActivity", "Received weeks: $weeks")
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (weeks.isNullOrEmpty()) {
                        android.util.Log.d("AttendanceListActivity", "No weeks found for course ${course.id}")
                        Toast.makeText(this@AttendanceListActivity, "Bu ders için henüz QR kod oluşturulmamış", Toast.LENGTH_LONG).show()
                        weekAdapter.updateWeeks(emptyList())
                    } else {
                        android.util.Log.d("AttendanceListActivity", "Found ${weeks.size} weeks with QR codes")
                        weeksWithQR = weeks
                        weekAdapter.updateWeeks(weeks)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AttendanceListActivity", "Error loading weeks: ${e.message}", e)
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@AttendanceListActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadAttendanceForWeek(week: WeekWithQR) {
        // Önce oturum seçimi yapılacak
        val course = selectedCourse
        if (course == null || course.uuid.isNullOrBlank()) {
            Toast.makeText(this, "Ders ID bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        val courseId = course.uuid ?: ""
        if (courseId.isBlank()) {
            Toast.makeText(this, "Ders ID bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        val courseIdLong = courseId.toLongOrNull()
        if (courseIdLong == null) {
            Toast.makeText(this, "Ders ID geçersiz", Toast.LENGTH_SHORT).show()
            return
        }
        
        // O hafta için oturumları çek ve seçim dialogu göster
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val sessionsResponse = apiService.getWeeklySessions(courseIdLong, week.week_number)
                val allSessions = sessionsResponse?.allSessions ?: emptyList()
                val totalSessions = sessionsResponse?.totalSessions ?: course.weeklyHours
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (allSessions.isEmpty() && totalSessions == 0) {
                        Toast.makeText(this@AttendanceListActivity, "Bu hafta için oturum bulunamadı", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }
                    
                    // Oturum seçimi dialogu göster
                    showSessionSelectionDialog(week, courseId, courseIdLong, allSessions, totalSessions)
                }
            } catch (e: Exception) {
                android.util.Log.e("AttendanceListActivity", "Error loading sessions: ${e.message}", e)
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    // Fallback: Tüm oturumlar için yoklama göster
                    loadAttendanceForWeekAndSession(week, courseId, courseIdLong, null, course.weeklyHours)
                }
            }
        }
    }
    
    private fun showSessionSelectionDialog(
        week: WeekWithQR,
        courseId: String,
        courseIdLong: Long,
        allSessions: List<WeeklySession>,
        totalSessions: Int
    ) {
        val sessionItems = mutableListOf<String>()
        val sessionNumbers = mutableListOf<Int?>()
        
        // "Tüm Oturumlar" seçeneği ekle
        sessionItems.add("Tüm Oturumlar")
        sessionNumbers.add(null)
        
        // Mevcut oturumları ekle
        if (allSessions.isNotEmpty()) {
            allSessions.forEach { session ->
                val sessionText = if (session.isCompleted) {
                    "${session.sessionNumber}. Oturum (Tamamlandı)"
                } else {
                    "${session.sessionNumber}. Oturum"
                }
                sessionItems.add(sessionText)
                sessionNumbers.add(session.sessionNumber)
            }
        } else {
            // Eğer API'den gelmediyse, 1'den totalSessions'a kadar oturumları oluştur
            for (i in 1..totalSessions) {
                sessionItems.add("$i. Oturum")
                sessionNumbers.add(i)
            }
        }
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Hafta ${week.week_number} - Oturum Seçimi")
            .setItems(sessionItems.toTypedArray()) { _, which ->
                val selectedSessionNumber = sessionNumbers[which]
                loadAttendanceForWeekAndSession(week, courseId, courseIdLong, selectedSessionNumber, totalSessions)
            }
            .setNegativeButton("İptal", null)
            .create()
        
        dialog.show()
    }
    
    private fun loadAttendanceForWeekAndSession(
        week: WeekWithQR,
        courseId: String,
        courseIdLong: Long,
        selectedSessionNumber: Int?,
        totalSessions: Int
    ) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                
                // 1. Tüm öğrencileri çek (derse kayıtlı)
                val allStudents = supabaseService.getAllStudentsForCourse(courseId) ?: emptyList()
                android.util.Log.d("AttendanceListActivity", "Found ${allStudents.size} students enrolled in course")
                allStudents.forEach { student ->
                    android.util.Log.d("AttendanceListActivity", "Student: ${student.studentId} - ${student.profiles?.fullName}")
                }
                
                // 2. Yoklama kayıtlarını çek
                var attendanceRecords = supabaseService.getAttendanceForWeek(courseId, week.week_number) ?: emptyList()
                android.util.Log.d("AttendanceListActivity", "Found ${attendanceRecords.size} attendance records (before filtering)")
                
                // Seçilen oturum numarasına göre filtrele
                if (selectedSessionNumber != null) {
                    attendanceRecords = attendanceRecords.filter { it.sessionNumber == selectedSessionNumber }
                    android.util.Log.d("AttendanceListActivity", "Filtered to ${attendanceRecords.size} records for session $selectedSessionNumber")
                } else {
                    android.util.Log.d("AttendanceListActivity", "Showing all sessions (no filter)")
                }
                
                // 4. Öğrencileri ve yoklama kayıtlarını birleştir
                // studentId'leri normalize et (String'e çevir, trim, lowercase) karşılaştırma için
                fun normalizeId(id: String): String {
                    return id.trim().lowercase().replace("-", "").replace("_", "")
                }
                
                // Öğrenci başına session bazlı yoklama kayıtlarını topla
                val attendanceByStudent = mutableMapOf<String, MutableList<AttendanceRecord>>()
                attendanceRecords.forEach { rec ->
                    val key = normalizeId(rec.studentId)
                    if (!attendanceByStudent.containsKey(key)) {
                        attendanceByStudent[key] = mutableListOf()
                    }
                    attendanceByStudent[key]?.add(rec)
                }
                
                // Email bazlı da topla (fallback için)
                val attendanceByEmail = mutableMapOf<String, MutableList<AttendanceRecord>>()
                attendanceRecords.forEach { rec ->
                    val emailKey = rec.profiles?.email?.trim()?.lowercase()
                    if (!emailKey.isNullOrBlank()) {
                        if (!attendanceByEmail.containsKey(emailKey)) {
                            attendanceByEmail[emailKey] = mutableListOf()
                        }
                        attendanceByEmail[emailKey]?.add(rec)
                    }
                }
                
                android.util.Log.d("AttendanceListActivity", "Attendance by student: ${attendanceByStudent.size}, by email: ${attendanceByEmail.size}")
                
                // Her öğrenci için tek satır üret (session bazlı bilgi ile)
                val combinedAttendance = allStudents.map { student ->
                    val normalizedId = normalizeId(student.studentId)
                    val studentAttendances = student.email.trim().lowercase().let { attendanceByEmail[it] }
                        ?: attendanceByStudent[normalizedId]
                        ?: emptyList()
                    
                    // Seçilen oturum numarasına göre filtrele
                    val filteredAttendances = if (selectedSessionNumber != null) {
                        studentAttendances.filter { it.sessionNumber == selectedSessionNumber }
                    } else {
                        studentAttendances
                    }
                    
                    // Katıldığı session numaralarını topla (seçilen oturum için)
                    val attendedSessions = if (selectedSessionNumber != null) {
                        // Belirli bir oturum seçildiyse, sadece o oturumu göster
                        if (filteredAttendances.isNotEmpty()) {
                            listOf(selectedSessionNumber)
                        } else {
                            emptyList()
                        }
                    } else {
                        // Tüm oturumlar seçildiyse, tüm session numaralarını göster
                        filteredAttendances
                            .mapNotNull { it.sessionNumber }
                            .distinct()
                            .sorted()
                    }
                    
                    // Eğer session_number null ise (eski sistem), 1 session sayılır
                    val hasLegacyAttendance = filteredAttendances.any { it.sessionNumber == null }
                    val finalAttendedSessions = if (hasLegacyAttendance && attendedSessions.isEmpty()) {
                        if (selectedSessionNumber != null) {
                            listOf(selectedSessionNumber) // Seçilen oturum için
                        } else {
                            listOf(1) // Eski sistem için 1 session sayılır
                        }
                    } else {
                        attendedSessions
                    }
                    
                    val hasAttendance = filteredAttendances.isNotEmpty()
                    val firstAttendance = filteredAttendances.firstOrNull()
                    
                    // totalSessions: Eğer belirli bir oturum seçildiyse 1, değilse toplam session sayısı
                    val displayTotalSessions = if (selectedSessionNumber != null) {
                        1
                    } else {
                        totalSessions
                    }
                    
                    val record = if (hasAttendance && firstAttendance != null) {
                        StudentRecord(
                            studentId = student.studentId,
                            email = student.email,
                            profiles = firstAttendance.profiles ?: student.profiles,
                            fullName = firstAttendance.profiles?.fullName ?: student.profiles?.fullName ?: "",
                            hasAttendance = true,
                            attendanceTime = firstAttendance.markedAt,
                            method = firstAttendance.method,
                            totalSessions = displayTotalSessions,
                            attendedSessions = finalAttendedSessions
                        )
                    } else {
                        StudentRecord(
                            studentId = student.studentId,
                            email = student.email,
                            profiles = student.profiles,
                            fullName = student.profiles?.fullName ?: "",
                            hasAttendance = false,
                            attendanceTime = null,
                            method = null,
                            totalSessions = displayTotalSessions,
                            attendedSessions = emptyList()
                        )
                    }
                    android.util.Log.d("AttendanceListActivity", "Combined row: ${record.studentId} - ${record.fullName} - hasAttendance=${record.hasAttendance} - sessions=${record.attendedSessions.size}/${record.totalSessions}")
                    record
                }.sortedBy { it.fullName }
                
                android.util.Log.d("AttendanceListActivity", "All students count: ${allStudents.size}")
                android.util.Log.d("AttendanceListActivity", "Attendance records count: ${attendanceRecords.size}")
                android.util.Log.d("AttendanceListActivity", "Combined attendance count: ${combinedAttendance.size}")
                
                // İstatistikleri hesapla
                totalStudents = combinedAttendance.size
                attendedStudents = combinedAttendance.count { it.hasAttendance }
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (allStudents.isEmpty()) {
                        Toast.makeText(this@AttendanceListActivity, "Bu derse kayıtlı öğrenci bulunamadı", Toast.LENGTH_SHORT).show()
                        binding.layoutAttendance.visibility = android.view.View.GONE
                    } else {
                        currentAttendance = combinedAttendance
                        android.util.Log.d("AttendanceListActivity", "Passing to adapter size=${combinedAttendance.size}")
                        android.util.Log.d("AttendanceListActivity", "RecyclerView visibility before: ${binding.recyclerViewAttendance.visibility}")
                        android.util.Log.d("AttendanceListActivity", "Layout attendance visibility before: ${binding.layoutAttendance.visibility}")
                        attendanceAdapter.updateAttendance(combinedAttendance)
                        binding.layoutAttendance.visibility = android.view.View.VISIBLE
                        binding.recyclerViewAttendance.visibility = android.view.View.VISIBLE
                        android.util.Log.d("AttendanceListActivity", "RecyclerView visibility after: ${binding.recyclerViewAttendance.visibility}")
                        android.util.Log.d("AttendanceListActivity", "Layout attendance visibility after: ${binding.layoutAttendance.visibility}")
                        android.util.Log.d("AttendanceListActivity", "RecyclerView adapter itemCount: ${attendanceAdapter.itemCount}")
                        binding.btnExportReport.visibility = android.view.View.VISIBLE
                        val sessionText = if (selectedSessionNumber != null) {
                            "Hafta ${week.week_number} - ${selectedSessionNumber}. Oturum - ${week.created_at.substring(0, 10)}"
                        } else {
                            "Hafta ${week.week_number} - Tüm Oturumlar - ${week.created_at.substring(0, 10)}"
                        }
                        binding.tvWeekTitle.text = sessionText
                        
                        // İstatistikleri güncelle
                        updateAttendanceStatistics()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("AttendanceListActivity", "Error loading attendance: ${e.message}", e)
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@AttendanceListActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun updateAttendanceStatistics() {
        val notAttended = totalStudents - attendedStudents
        val attendanceRate = if (totalStudents > 0) {
            (attendedStudents * 100.0 / totalStudents).toInt()
        } else {
            0
        }
        
        binding.tvAttendanceStats.text = "Toplam: $totalStudents | Katılan: $attendedStudents | Katılmayan: $notAttended | Oran: %$attendanceRate"
    }
    
    private fun exportAttendanceReport() {
        if (selectedCourse == null || currentAttendance.isEmpty()) {
            Toast.makeText(this, "Dışa aktarılacak yoklama verisi bulunamadı", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val csvContent = StringBuilder()
            
            // CSV Header
            csvContent.append("Öğrenci Adı Soyadı,Öğrenci ID,Yoklama Saati,Yoklama Yöntemi\n")
            
            // CSV Data
            currentAttendance.forEach { attendance ->
                val studentName = attendance.fullName.ifBlank { attendance.profiles?.fullName ?: "Bilinmeyen" }
                val markedTime = attendance.attendanceTime ?: ""
                val method = attendance.method ?: if (attendance.hasAttendance) "qr" else ""
                
                csvContent.append("\"$studentName\",\"${attendance.studentId}\",\"$markedTime\",\"$method\"\n")
            }
            
            // Create file
            val fileName = "Yoklama_Raporu_${selectedCourse!!.code}_${System.currentTimeMillis()}.csv"
            val file = java.io.File(getExternalFilesDir(null), fileName)
            file.writeText(csvContent.toString())
            
            // Share file
            shareFile(file, fileName)
            
            Toast.makeText(this, "Rapor oluşturuldu: $fileName", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "Dosya oluşturulurken hata: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun shareFile(file: java.io.File, fileName: String) {
        try {
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Yoklama Raporu - $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            startActivity(Intent.createChooser(shareIntent, "Raporu Paylaş"))
            
        } catch (e: Exception) {
            Toast.makeText(this, "Dosya paylaşılırken hata: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
