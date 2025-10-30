package com.smartattendance.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class AttendanceAdapter : RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder>() {
    
    private var attendance: List<AttendanceRecord> = emptyList()
    
    fun updateAttendance(newAttendance: List<AttendanceRecord>) {
        attendance = newAttendance
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_attendance, parent, false)
        return AttendanceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val record = attendance[position]
        holder.bind(record)
    }
    
    override fun getItemCount(): Int = attendance.size
    
    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStudentName: TextView = itemView.findViewById(R.id.tv_student_name)
        private val tvAttendanceTime: TextView = itemView.findViewById(R.id.tv_attendance_time)
        
        fun bind(record: AttendanceRecord) {
            tvStudentName.text = record.profiles?.fullName ?: "Bilinmeyen Öğrenci"
            
            val time = try {
                // Handle ISO (with T/Z) and Postgres (space + offset) formats
                val str = record.markedAt
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
                try { record.markedAt.substring(11, 16) } catch (_: Exception) { record.markedAt }
            }
            
            tvAttendanceTime.text = "Saat: $time - ${record.method}"
        }
    }
}
