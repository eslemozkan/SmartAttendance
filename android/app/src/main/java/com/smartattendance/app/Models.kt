package com.smartattendance.app

data class Course(
    val id: Int,
    val name: String,
    val code: String,
    val schedule: String
)

data class Week(
    val id: Int,
    val name: String
)

data class AttendanceRecord(
    val studentName: String,
    val time: String,
    val status: String
)
