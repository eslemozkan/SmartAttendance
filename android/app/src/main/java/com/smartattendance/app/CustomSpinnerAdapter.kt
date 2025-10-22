package com.smartattendance.app

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomSpinnerAdapter(
    context: Context,
    private val items: List<String>,
    private val textColor: Int = Color.parseColor("#424242") // academic_secondary - daha açık gri
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.setTextColor(textColor)
        textView.textSize = 16f
        textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.setTextColor(textColor)
        textView.textSize = 16f
        textView.typeface = android.graphics.Typeface.DEFAULT_BOLD
        textView.setPadding(16, 16, 16, 16)
        return view
    }
}
