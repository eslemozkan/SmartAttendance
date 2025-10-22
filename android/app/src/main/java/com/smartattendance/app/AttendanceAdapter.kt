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
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return AttendanceViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        val record = attendance[position]
        holder.bind(record)
    }
    
    override fun getItemCount(): Int = attendance.size
    
    inner class AttendanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)
        
        fun bind(record: AttendanceRecord) {
            text1.text = record.student_name
            text1.setTextColor(itemView.context.getColor(com.smartattendance.app.R.color.text_primary))
            text1.textSize = 16f
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = try {
                timeFormat.format(Date(record.marked_at))
            } catch (e: Exception) {
                record.marked_at.substring(11, 16)
            }
            
            text2.text = "Saat: $time - ${record.method}"
            text2.setTextColor(itemView.context.getColor(com.smartattendance.app.R.color.text_secondary))
            text2.textSize = 14f
        }
    }
}
