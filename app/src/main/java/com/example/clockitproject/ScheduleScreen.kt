package com.example.clockitproject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import android.app.TimePickerDialog


// Data class for each day cell
data class DayOfWeekItem(
    val label: String,
    val date: Int,
    val fullDate: LocalDate
)

@Composable
fun ScheduleScreen(taskViewModel: TaskViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var weekStart by remember {
        mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)))
    }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val weekDays: List<DayOfWeekItem> = (0..6).map { offset ->
        val d = weekStart.plusDays(offset.toLong())
        DayOfWeekItem(
            label = d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            date = d.dayOfMonth,
            fullDate = d
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFBFE1D6), Color(0xFF9DBFAD))
                )
            )
    ) {
        Scaffold(
            topBar = {
                MonthNavigator(
                    month = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        .replaceFirstChar { it.uppercaseChar() },
                    year = currentMonth.year,
                    onPrevious = { currentMonth = currentMonth.minusMonths(1) },
                    onNext = { currentMonth = currentMonth.plusMonths(1) }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Task", tint = Color.Black)
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = { BottomBar() },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Spacer(Modifier.height(8.dp))
                WeekStrip(
                    days = weekDays,
                    selected = selectedDate,
                    onDaySelected = { selectedDate = it.fullDate },
                    onPrevWeek = { weekStart = weekStart.minusWeeks(1) },
                    onNextWeek = { weekStart = weekStart.plusWeeks(1) }
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Today's Tasks:",
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
                TaskList(
                    tasks = taskViewModel.tasks.filter { task -> task.date == selectedDate },
                    modifier = Modifier.weight(1f),
                    onDelete = { taskViewModel.removeTask(it) }
                )
            }
        }

        if (showDialog) {
            AddTaskDialog(
                defaultDate = selectedDate,
                onAdd = { task ->
                    taskViewModel.addTask(task)
                    showDialog = false
                },
                onDismiss = { showDialog = false }
            )
        }
    }
}

@Composable
fun BottomBar() {}

@Composable
fun TaskItem(task: Task, modifier: Modifier = Modifier, onDelete: (Task) -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBFE1D6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                Text(text = task.location, fontSize = 12.sp, color = Color.DarkGray)
                Text(text = "${task.startTime} – ${task.endTime}", fontSize = 13.sp, color = Color.Black)
            }
            IconButton(onClick = { onDelete(task) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Red)
            }
        }
    }
}

@Composable
fun TaskList(tasks: List<Task>, modifier: Modifier = Modifier, onDelete: (Task) -> Unit) {
    LazyColumn(modifier) {
        items(tasks) { task ->
            TaskItem(task = task, onDelete = onDelete)
        }
    }
}

@Composable
fun AddTaskDialog(
    defaultDate: LocalDate,
    onAdd: (Task) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var selectedReminder by remember { mutableStateOf("15 min") }
    val repeatDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    val showStartPicker = remember { mutableStateOf(false) }
    val showEndPicker = remember { mutableStateOf(false) }

    if (showStartPicker.value) {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                startTime = "%02d:%02d".format(hour, minute)
                showStartPicker.value = false
            },
            9, 0, false
        ).show()
    }

    if (showEndPicker.value) {
        TimePickerDialog(
            context,
            { _, hour: Int, minute: Int ->
                endTime = "%02d:%02d".format(hour, minute)
                showEndPicker.value = false
            },
            10, 0, false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Repeat Days
                var showDaySelector by remember { mutableStateOf(false) }

                Button(
                    onClick = { showDaySelector = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(
                        if (selectedDays.isEmpty()) "Select Days"
                        else "Days: ${selectedDays.joinToString(", ")}"
                    )
                }

                if (showDaySelector) {
                    AlertDialog(
                        onDismissRequest = { showDaySelector = false },
                        title = { Text("Choose Repeat Days") },
                        text = {
                            Column {
                                repeatDays.forEach { day ->
                                    val isSelected = selectedDays.contains(day)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                            }
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = {
                                                selectedDays = if (isSelected) selectedDays - day else selectedDays + day
                                            }
                                        )
                                        Text(text = day)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showDaySelector = false }) {
                                Text("Done")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDaySelector = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }


                Spacer(Modifier.height(8.dp))

                // Reminders
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder:", modifier = Modifier.padding(end = 8.dp))
                    RadioButton(
                        selected = selectedReminder == "15 min",
                        onClick = { selectedReminder = "15 min" }
                    )
                    Text("15 min")
                    Spacer(Modifier.width(8.dp))
                    RadioButton(
                        selected = selectedReminder == "30 min",
                        onClick = { selectedReminder = "30 min" }
                    )
                    Text("30 min")
                }

                // Start Time
                Button(
                    onClick = { showStartPicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(if (startTime.isEmpty()) "Select Start Time" else "Start: $startTime")
                }

                // End Time
                Button(
                    onClick = { showEndPicker.value = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(if (endTime.isEmpty()) "Select End Time" else "End: $endTime")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onAdd(Task(title, location, startTime, endTime, defaultDate))
                onDismiss()
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun MonthNavigator(
    month: String,
    year: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Month")
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = "$month $year",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Next Month")
        }
    }
}

@Composable
fun WeekStrip(
    days: List<DayOfWeekItem>,
    selected: LocalDate,
    onDaySelected: (DayOfWeekItem) -> Unit,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrevWeek) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Week")
        }
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            days.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onDaySelected(day) }
                ) {
                    Text(day.label, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        day.date.toString(),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (day.fullDate == selected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }
        }
        IconButton(onClick = onNextWeek) {
            Icon(Icons.Filled.ArrowForward, contentDescription = "Next Week")
        }
    }
}
