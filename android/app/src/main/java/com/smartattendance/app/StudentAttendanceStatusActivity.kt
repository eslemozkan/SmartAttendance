package com.smartattendance.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.smartattendance.app.databinding.ActivityStudentAttendanceStatusBinding
import kotlinx.coroutines.launch

class StudentAttendanceStatusActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStudentAttendanceStatusBinding
    private val apiService = ApiService()
    private lateinit var adapter: StudentAttendanceAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentAttendanceStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        loadAttendanceStatus()
    }
    
    private fun setupUI() {
        // RecyclerView setup
        adapter = StudentAttendanceAdapter()
        binding.recyclerViewCourses.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewCourses.adapter = adapter
        
        // Back button
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        // Refresh button
        binding.btnRefresh.setOnClickListener {
            loadAttendanceStatus()
        }
    }
    
    private fun loadAttendanceStatus() {
        val email = intent.getStringExtra("email") ?: ""
        if (email.isBlank()) {
            Toast.makeText(this, "Email bilgisi bulunamadı", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.recyclerViewCourses.visibility = android.view.View.GONE
        binding.tvEmpty.visibility = android.view.View.GONE
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("StudentAttendanceStatus", "Loading attendance for email: $email")
                val courses = apiService.getStudentAttendanceStatus(email)
                
                android.util.Log.d("StudentAttendanceStatus", "Received ${courses?.size ?: 0} courses")
                
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    
                    if (courses == null) {
                        binding.recyclerViewCourses.visibility = android.view.View.GONE
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                        binding.tvEmpty.text = "Yoklama durumu yüklenirken bir hata oluştu. Lütfen tekrar deneyin."
                        Toast.makeText(this@StudentAttendanceStatusActivity, "Veri yüklenirken hata oluştu", Toast.LENGTH_LONG).show()
                    } else if (courses.isEmpty()) {
                        binding.recyclerViewCourses.visibility = android.view.View.GONE
                        binding.tvEmpty.visibility = android.view.View.VISIBLE
                        binding.tvEmpty.text = "Henüz ders kaydınız bulunmuyor veya QR kod oluşturulmamış.\n\nLütfen logcat çıktısını kontrol edin (ApiService tag'i)."
                    } else {
                        binding.recyclerViewCourses.visibility = android.view.View.VISIBLE
                        binding.tvEmpty.visibility = android.view.View.GONE
                        adapter.updateCourses(courses)
                        android.util.Log.d("StudentAttendanceStatus", "Displaying ${courses.size} courses")
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentAttendanceStatus", "Error loading attendance: ${e.message}", e)
                android.util.Log.e("StudentAttendanceStatus", "Stack trace: ${e.stackTraceToString()}")
                runOnUiThread {
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.recyclerViewCourses.visibility = android.view.View.GONE
                    binding.tvEmpty.visibility = android.view.View.VISIBLE
                    binding.tvEmpty.text = "Yoklama durumu yüklenirken hata oluştu: ${e.message}\n\nLütfen logcat çıktısını kontrol edin."
                    Toast.makeText(this@StudentAttendanceStatusActivity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

