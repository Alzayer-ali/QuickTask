package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Priority
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuickCaptureUiState(
    val title: String = "",
    val notes: String = "",
    val dueDateMillis: Long? = DateTimeUtils.getTodayStartMillis(),
    val dueTimeHour: Int? = null,
    val dueTimeMinute: Int? = null,
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)

class QuickCaptureViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickCaptureUiState())
    val uiState: StateFlow<QuickCaptureUiState> = _uiState.asStateFlow()

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onDueDateChange(dateMillis: Long?) {
        _uiState.update { it.copy(dueDateMillis = dateMillis) }
    }

    fun onDueTimeChange(hour: Int?, minute: Int?) {
        _uiState.update { it.copy(dueTimeHour = hour, dueTimeMinute = minute) }
    }

    fun onPriorityChange(priority: Priority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun saveTask(onComplete: () -> Unit) {
        val currentState = _uiState.value
        val cleanTitle = currentState.title.trim()
        if (cleanTitle.isEmpty()) return

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = cleanTitle,
                    notes = currentState.notes.trim(),
                    dueDateMillis = currentState.dueDateMillis,
                    dueTimeHour = currentState.dueTimeHour,
                    dueTimeMinute = currentState.dueTimeMinute,
                    priority = currentState.priority,
                    category = currentState.category,
                    isCompleted = false
                )
            )
            _uiState.update { it.copy(isSaving = false, isSaved = true) }
            onComplete()
        }
    }

    companion object {
        fun provideFactory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return QuickCaptureViewModel(repository) as T
                }
            }
    }
}
