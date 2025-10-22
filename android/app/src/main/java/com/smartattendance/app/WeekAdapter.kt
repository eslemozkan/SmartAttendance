package com.smartattendance.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WeekAdapter(
    private val onWeekClick: (WeekWithQR) -> Unit
) : RecyclerView.Adapter<WeekAdapter.WeekViewHolder>() {
    
    private var weeks: List<WeekWithQR> = emptyList()
    
    fun updateWeeks(newWeeks: List<WeekWithQR>) {
        weeks = newWeeks
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return WeekViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: WeekViewHolder, position: Int) {
        val week = weeks[position]
        holder.bind(week)
    }
    
    override fun getItemCount(): Int = weeks.size
    
    inner class WeekViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(android.R.id.text1)
        
        fun bind(week: WeekWithQR) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = try {
                dateFormat.format(Date(week.created_at))
            } catch (e: Exception) {
                week.created_at.substring(0, 10)
            }
            
            textView.text = "Hafta ${week.week_number} - $date"
            textView.setTextColor(itemView.context.getColor(com.smartattendance.app.R.color.academic_blue))
            textView.textSize = 16f
            
            itemView.setOnClickListener {
                onWeekClick(week)
            }
        }
    }
}
