package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.CourseWithLessons

sealed interface CourseDetailUiState {
    data object Loading : CourseDetailUiState

    data class Success(
        val courseWithLessons: CourseWithLessons,
        val isSyncing: Boolean = false,
        val isOnline: Boolean = true,
        val syncMessage: String? = null
    ) : CourseDetailUiState

    data class Error(
        val message: String
    ) : CourseDetailUiState
}
