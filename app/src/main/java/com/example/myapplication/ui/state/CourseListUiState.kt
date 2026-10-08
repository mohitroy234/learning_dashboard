package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.Course

sealed interface CourseListUiState {
    data object Loading : CourseListUiState

    data class Success(
        val courses: List<Course>,
        val isSyncing: Boolean = false,
        val isOnline: Boolean = true,
        val userMessage: String? = null
    ) : CourseListUiState

    data class Error(
        val message: String
    ) : CourseListUiState
}
