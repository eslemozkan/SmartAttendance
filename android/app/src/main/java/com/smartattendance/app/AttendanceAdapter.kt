package com.smartattendance.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.smartattendance.app.StudentRecord
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {
    
    private var attendance: List<StudentRecord> = emptyList()
    
    fun updateAttendance(newAttendance: List<StudentRecord>) {
        attendance = newAttendance
        Log.d(
            "AttendanceAdapter",
            "updateAttendance size=${newAttendance.size}, items=${
                newAttendance.joinToString { item -> "${item.fullName}|${item.email}" }
            }"
        )
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        Log.d("AttendanceAdapter", "onCreateViewHolder called")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        Log.d("AttendanceAdapter", "onBindViewHolder called for position=$position, totalItems=${attendance.size}")
        if (position >= attendance.size) {
            Log.e("AttendanceAdapter", "Position $position out of bounds! Size=${attendance.size}")
            return
        }
        val record = attendance[position]
        Log.d("AttendanceAdapter", "Binding record: ${record.fullName} - ${record.email} - hasAttendance=${record.hasAttendance}")
        holder.bind(record)
    }
    
    override fun getItemCount(): Int {
        val count = attendance.size
        Log.d("AttendanceAdapter", "getItemCount called: $count")
        return count
    }
    
    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        private val tvStudentEmail: TextView = itemView.findViewById(R.id.tv_student_email)
        private val tvAttendanceTime: TextView = itemView.findViewById(R.id.tv_attendance_time)
        private val tvStatusBadge: TextView = itemView.findViewById(R.id.tv_status_badge)
        private val iconPerson: android.widget.ImageView = itemView.findViewById(R.id.icon_person)
        
        fun bind(record: StudentRecord) {
            tvStudentName.text = record.fullName.ifBlank { record.profiles?.fullName ?: "Bilinmeyen Öğrenci" }
            val emailText = record.email.ifBlank { record.profiles?.email ?: "" }
            if (emailText.isNotBlank()) {
                tvStudentEmail.visibility = View.VISIBLE
                tvStudentEmail.text = emailText
            } else {
                tvStudentEmail.visibility = View.GONE
            }
            
            if (record.hasAttendance) {
                // Yoklamaya katılan öğrenci
                val time = try {
                    // Handle ISO (with T/Z) and Postgres (space + offset) formats
                    val str = record.attendanceTime ?: ""
                    val parsed = try {
                        java.time.Instant.parse(str)
                    } catch (e: Exception) {
                        // Convert "YYYY-MM-DD HH:MM:SS+00" to ISO by replacing space with 'T'
                        val fixed = str.replace(' ', 'T')
                        java.time.OffsetDateTime.parse(fixed)
                            .toInstant()
                    }
                    val zdt = parsed.atZone(java.time.ZoneId.systemDefault())
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm").format(zdt)
                } catch (e: Exception) {
                    try { record.attendanceTime?.substring(11, 16) ?: "" } catch (_: Exception) { record.attendanceTime ?: "" }
                }
                
                // Session bazlı bilgi göster
                val sessionInfo = if (record.totalSessions > 0 && record.attendedSessions.isNotEmpty()) {
                    val sessionText = if (record.attendedSessions.size == record.totalSessions) {
                        "Tüm oturumlar (${record.attendedSessions.size}/${record.totalSessions})"
                    } else {
                        "Oturumlar: ${record.attendedSessions.joinToString(", ")} (${record.attendedSessions.size}/${record.totalSessions})"
                    }
                    if (time.isNotBlank()) {
                        "$sessionText - Saat: $time"
                    } else {
                        sessionText
                    }
                } else if (time.isNotBlank()) {
                    "Saat: $time"
                } else {
                    "Yoklama var"
                }
                
                tvAttendanceTime.text = sessionInfo
                tvAttendanceTime.visibility = android.view.View.VISIBLE
                tvStatusBadge.text = "VAR"
                tvStatusBadge.setBackgroundResource(R.drawable.badge_primary)
                tvStatusBadge.setTextColor(android.graphics.Color.WHITE)
                iconPerson.setColorFilter(itemView.context.getColor(R.color.academic_success))
            } else {
                // Yoklamaya katılmayan öğrenci
                val noAttendanceText = if (record.totalSessions > 0) {
                    "Yoklamaya katılmadı (0/${record.totalSessions} oturum)"
                } else {
                    "Yoklamaya katılmadı"
                }
                tvAttendanceTime.text = noAttendanceText
                tvAttendanceTime.visibility = android.view.View.VISIBLE
                tvStatusBadge.text = "YOK"
                tvStatusBadge.setBackgroundResource(R.drawable.badge_error)
                tvStatusBadge.setTextColor(android.graphics.Color.WHITE)
                iconPerson.setColorFilter(itemView.context.getColor(R.color.academic_error))
            }
        }
    }
}
