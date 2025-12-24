package com.smartattendance.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartattendance.app.R
import java.text.SimpleDateFormat
import java.util.*

class StudentAttendanceAdapter : RecyclerView.Adapter<StudentAttendanceAdapter.CourseViewHolder>() {
    private var courses: List<StudentCourseWithWeeks> = emptyList()
    
    fun updateCourses(courses: List<StudentCourseWithWeeks>) {
        this.courses = courses
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_student_course, parent, false)
        return CourseViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.bind(course)
    }
    
    override fun getItemCount(): Int = courses.size
    
    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCourseName: TextView = itemView.findViewById(R.id.tvCourseName)
        private val tvCourseCode: TextView = itemView.findViewById(R.id.tvCourseCode)
        private val tvTotalWeeks: TextView = itemView.findViewById(R.id.tvTotalWeeks)
        private val tvAttendance: TextView = itemView.findViewById(R.id.tvAttendance)
        private val tvAttendanceRate: TextView = itemView.findViewById(R.id.tvAttendanceRate)
        private val tvNoWeeks: TextView = itemView.findViewById(R.id.tvNoWeeks)
        private val llAttendanceInfo: View = itemView.findViewById(R.id.llAttendanceInfo)
        
        fun bind(course: StudentCourseWithWeeks) {
            tvCourseName.text = course.courseName
            tvCourseCode.text = course.courseCode ?: ""
            
            if (course.weeks.isEmpty()) {
                tvNoWeeks.visibility = View.VISIBLE
                llAttendanceInfo.visibility = View.GONE
            } else {
                tvNoWeeks.visibility = View.GONE
                llAttendanceInfo.visibility = View.VISIBLE
                
                val totalWeeks = course.weeks.size
                val attendedWeeks = course.weeks.count { it.hasAttendance }
                val attendanceRate = if (totalWeeks > 0) {
                    (attendedWeeks * 100 / totalWeeks)
                } else 0
                
                tvTotalWeeks.text = totalWeeks.toString()
                tvAttendance.text = "$attendedWeeks/$totalWeeks"
                tvAttendanceRate.text = "%$attendanceRate"
                
                // Renk kodlama
                when {
                    attendanceRate >= 80 -> tvAttendanceRate.setTextColor(0xFF4CAF50.toInt()) // Green
                    attendanceRate >= 60 -> tvAttendanceRate.setTextColor(0xFFFF9800.toInt()) // Orange
                    else -> tvAttendanceRate.setTextColor(0xFFF44336.toInt()) // Red
                }
            }
            
            // Click listener to show week details
            itemView.setOnClickListener {
                showWeekDetailsDialog(course)
            }
        }
        
        private fun showWeekDetailsDialog(course: StudentCourseWithWeeks) {
            val context = itemView.context
            val dialog = android.app.AlertDialog.Builder(context)
            dialog.setTitle("${course.courseName} - Haftalık Detay")
            
            val weekDetails = course.weeks.map { week ->
                val status = if (week.hasAttendance) {
                    val time = week.attendanceTime?.let { formatDateTime(it) } ?: ""
                    // Session bazlı bilgi göster
                    if (week.totalSessions > 0) {
                        val sessionInfo = if (week.attendedSessions.isNotEmpty()) {
                            " (${week.attendedSessions.size}/${week.totalSessions} oturum: ${week.attendedSessions.joinToString(", ")})"
                        } else {
                            " (0/${week.totalSessions} oturum)"
                        }
                        "Hafta ${week.weekNumber}: ✅ VAR$sessionInfo $time"
                    } else {
                        "Hafta ${week.weekNumber}: ✅ VAR $time"
                    }
                } else {
                    // Session bilgisi varsa göster
                    if (week.totalSessions > 0) {
                        "Hafta ${week.weekNumber}: ❌ YOK (0/${week.totalSessions} oturum)"
                    } else {
                        "Hafta ${week.weekNumber}: ❌ YOK"
                    }
                }
                status
            }
            
            if (weekDetails.isEmpty()) {
                dialog.setMessage("Henüz QR kod oluşturulmamış hafta bulunmuyor.")
            } else {
                dialog.setItems(weekDetails.toTypedArray()) { dialog, _ ->
                    dialog.dismiss()
                }
            }
            
            dialog.setPositiveButton("Kapat") { dialog, _ ->
                dialog.dismiss()
            }
            
            dialog.show()
        }
        
        private fun formatDateTime(dateTimeString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateTimeString)
                date?.let { outputFormat.format(it) } ?: dateTimeString
            } catch (e: Exception) {
                dateTimeString
            }
        }
    }
}

