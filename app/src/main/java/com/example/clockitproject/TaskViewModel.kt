package com.example.clockitproject

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.clockitproject.Task
import java.time.LocalDate


class TaskViewModel : ViewModel() {
    private val _tasks = mutableStateListOf<Task>()
    val tasks: List<Task> get() = _tasks

    init {
        _tasks.addAll(
            listOf(
                Task(
                    "CSC 430",
                    "College of Staten Island, Room 1N 118",
                    "9:05",
                    "12:05",
                    LocalDate.now()
                ),
                Task(
                    "CSC 330",
                    "College of Staten Island, Room 1N 118",
                    "2:05",
                    "3:15",
                    LocalDate.now()
                )
            )
        )
    }

    fun addTask(task: Task) {
        _tasks.add(task)
    }

    fun removeTask(task: Task) {
        _tasks.remove(task)
    }
}