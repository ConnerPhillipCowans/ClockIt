package com.example.clockitproject

data class Task(
    val title: String,
    val location: String,
    val startTime: String,
    val endTime: String,
    val date: java.time.LocalDate
)