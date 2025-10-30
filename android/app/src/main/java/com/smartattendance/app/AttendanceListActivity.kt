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
        Course(4, "Ders Yok", "N/A", "Bu hafta ders yapılmayacak")
    )
    
    private var selectedCourse: Course? = null
    private var weeksWithQR: List<WeekWithQR> = emptyList()
    private var currentAttendance: List<AttendanceRecord> = emptyList()

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
                val mapped: List<Course> = (assigned ?: emptyList()).mapNotNull { row ->
                    val id = row.courseId?.toInt() ?: return@mapNotNull null
                    val name = row.courseName ?: return@mapNotNull null
                    val code = row.courseCode ?: ""
                    Course(id, name, code, "")
                }
                if (mapped.isNotEmpty()) {
                    courses = mapped + courses.filter { it.id == 4 }
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
                if (selectedCourse?.id != 4) {
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
                val weeks = supabaseService.getWeeksWithQR(course.id)
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
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val attendance = supabaseService.getAttendanceForWeek(selectedCourse!!.id, week.week_number)
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (attendance.isNullOrEmpty()) {
                        Toast.makeText(this@AttendanceListActivity, "Bu hafta için yoklama verisi bulunamadı", Toast.LENGTH_SHORT).show()
                        binding.layoutAttendance.visibility = android.view.View.GONE
                    } else {
                        currentAttendance = attendance
                        attendanceAdapter.updateAttendance(attendance)
                        binding.layoutAttendance.visibility = android.view.View.VISIBLE
                        binding.btnExportReport.visibility = android.view.View.VISIBLE
                        binding.tvWeekTitle.text = "Hafta ${week.week_number} - ${week.created_at.substring(0, 10)}"
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    Toast.makeText(this@AttendanceListActivity, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
                val studentName = attendance.profiles?.fullName ?: "Bilinmeyen"
                val markedTime = attendance.markedAt
                val method = attendance.method
                
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
