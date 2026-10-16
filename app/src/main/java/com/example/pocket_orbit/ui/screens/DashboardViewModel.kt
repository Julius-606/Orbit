// ==========================================
// IDENTITY: The Translator / ViewModel
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardViewModel.kt
// VERSION: 1.0.1
// ==========================================

package com.example.pocket_orbit.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket_orbit.data.OrbitRepository
import com.example.pocket_orbit.data.StudyTaskEntity
import com.example.pocket_orbit.network.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel(
    private val repository: OrbitRepository
) : ViewModel() {

    val pendingTasks: StateFlow<List<StudyTaskEntity>> = repository.pendingTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTasks: StateFlow<List<StudyTaskEntity>> = repository.allTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    // Calendar state: Selected Date (Year, Month 0-11, Day 1-31)
    private val todayCal = Calendar.getInstance()
    private val _displayedYear = MutableStateFlow(todayCal.get(Calendar.YEAR))
    val displayedYear: StateFlow<Int> = _displayedYear

    private val _displayedMonth = MutableStateFlow(todayCal.get(Calendar.MONTH)) // 0 = Jan
    val displayedMonth: StateFlow<Int> = _displayedMonth

    private val _selectedDateKey = MutableStateFlow(formatDateKey(todayCal.time))
    val selectedDateKey: StateFlow<String> = _selectedDateKey

    // Fast O(1) Map of "yyyy-MM-dd" -> List of Tasks for that day
    val tasksByDateMap: StateFlow<Map<String, List<StudyTaskEntity>>> = allTasks.map { tasks ->
        val map = mutableMapOf<String, MutableList<StudyTaskEntity>>()
        for (task in tasks) {
            val key = task.dueDate?.let { formatDateKey(it) } ?: "no_date"
            map.getOrPut(key) { mutableListOf() }.add(task)
        }
        map
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Filtered tasks for the selected date on the calendar
    val selectedDateTasks: StateFlow<List<StudyTaskEntity>> = combine(
        allTasks,
        _selectedDateKey
    ) { tasks, key ->
        tasks.filter { task ->
            val taskKey = task.dueDate?.let { formatDateKey(it) } ?: "no_date"
            taskKey == key
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        refreshData()
    }

    fun refreshData() {
        syncTasks()
    }

    fun syncTasks() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshTasksFromVM()
            _isRefreshing.value = false
        }
    }

    fun setSelectedDate(calendar: Calendar) {
        _displayedYear.value = calendar.get(Calendar.YEAR)
        _displayedMonth.value = calendar.get(Calendar.MONTH)
        _selectedDateKey.value = formatDateKey(calendar.time)
    }

    fun setSelectedDate(year: Int, month: Int, day: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        _displayedYear.value = year
        _displayedMonth.value = month
        _selectedDateKey.value = formatDateKey(cal.time)
    }

    fun selectToday() {
        val now = Calendar.getInstance()
        setSelectedDate(now)
    }

    fun changeMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _displayedYear.value)
            set(Calendar.MONTH, _displayedMonth.value)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, delta)
        }
        _displayedYear.value = cal.get(Calendar.YEAR)
        _displayedMonth.value = cal.get(Calendar.MONTH)
    }

    fun createTask(
        title: String,
        subject: String,
        brainRotLevel: String = "chill",
        dueDate: Date? = null,
        isReminder: Boolean = false,
        remarks: String? = null,
        context: Context? = null
    ) {
        viewModelScope.launch {
            val task = repository.createTask(
                title = title.trim(),
                subject = subject.trim(),
                brainRotLevel = brainRotLevel,
                dueDate = dueDate,
                isReminder = isReminder,
                remarks = remarks
            )

            // If reminder requested and has a future due date, schedule system alarm
            if (isReminder && dueDate != null && context != null && dueDate.time > System.currentTimeMillis()) {
                NotificationHelper.scheduleAlarm(
                    context = context,
                    taskId = task.id,
                    title = task.title,
                    subject = task.subject,
                    triggerAtMillis = dueDate.time
                )
            }
        }
    }

    fun deleteTask(taskId: Int, context: Context? = null) {
        viewModelScope.launch {
            if (context != null) {
                NotificationHelper.cancelAlarm(context, taskId)
            }
            repository.deleteTask(taskId)
        }
    }

    fun markTaskCompleted(taskId: Int, context: Context? = null) {
        viewModelScope.launch {
            if (context != null) {
                NotificationHelper.cancelAlarm(context, taskId)
            }
            repository.markTaskComplete(taskId)
        }
    }

    companion object {
        fun formatDateKey(date: Date): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            return sdf.format(date)
        }
    }
}