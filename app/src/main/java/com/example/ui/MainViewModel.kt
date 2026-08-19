package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Priority
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    UPCOMING("Upcoming"),
    HIGH_PRIORITY("High Priority"),
    COMPLETED("Completed")
}

data class DashboardUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val totalPendingCount: Int = 0,
    val totalCompletedCount: Int = 0,
    val dueTodayCount: Int = 0,
    val selectedFilter: TaskFilter = TaskFilter.ALL,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val availableCategories: List<String> = listOf("General", "Work", "Personal", "Urgent", "Ideas"),
    val isCalendarSyncEnabled: Boolean = true,
    val calendarSyncStatus: String = "Ready"
)

class MainViewModel(
    val repository: TaskRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(TaskFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private var recentlyDeletedTask: TaskEntity? = null

    private val filterStateFlow = combine(
        _selectedFilter,
        _searchQuery,
        _selectedCategory
    ) { filter, query, categoryFilter ->
        Triple(filter, query, categoryFilter)
    }

    private val calendarSyncStateFlow = combine(
        repository.calendarSyncManager.isAutoSyncEnabled,
        repository.calendarSyncManager.statusMessage
    ) { isEnabled, status ->
        Pair(isEnabled, status)
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allTasks,
        filterStateFlow,
        calendarSyncStateFlow
    ) { allTasks, (filter, query, categoryFilter), (isCalendarEnabled, syncStatus) ->
        val pendingCount = allTasks.count { !it.isCompleted }
        val completedCount = allTasks.count { it.isCompleted }
        val todayCount = allTasks.count { !it.isCompleted && DateTimeUtils.isDueToday(it.dueDateMillis) }

        val filtered = allTasks.filter { task ->
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> !task.isCompleted && DateTimeUtils.isDueToday(task.dueDateMillis)
                TaskFilter.UPCOMING -> !task.isCompleted && DateTimeUtils.isUpcoming(task.dueDateMillis)
                TaskFilter.HIGH_PRIORITY -> !task.isCompleted && task.priority == Priority.HIGH
                TaskFilter.COMPLETED -> task.isCompleted
            }

            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.notes.contains(query, ignoreCase = true) ||
                    task.category.contains(query, ignoreCase = true)

            val matchesCategory = categoryFilter == null || task.category.equals(categoryFilter, ignoreCase = true)

            matchesFilter && matchesQuery && matchesCategory
        }

        DashboardUiState(
            tasks = filtered,
            totalPendingCount = pendingCount,
            totalCompletedCount = completedCount,
            dueTodayCount = todayCount,
            selectedFilter = filter,
            searchQuery = query,
            selectedCategory = categoryFilter,
            isCalendarSyncEnabled = isCalendarEnabled,
            calendarSyncStatus = syncStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    fun setFilter(filter: TaskFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        recentlyDeletedTask = task
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun undoDelete() {
        recentlyDeletedTask?.let { task ->
            viewModelScope.launch {
                repository.insertTask(task)
                recentlyDeletedTask = null
            }
        }
    }

    fun saveOrUpdateTask(task: TaskEntity) {
        viewModelScope.launch {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
        }
    }

    companion object {
        fun provideFactory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return MainViewModel(repository) as T
                }
            }
    }
}
