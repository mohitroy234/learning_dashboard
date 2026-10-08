package com.example.myapplication.ui.viewmodel.course

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.network.NetworkMonitor
import com.example.myapplication.domain.model.CourseWithLessons
import com.example.myapplication.domain.usecase.GetCourseDetailsUseCase
import com.example.myapplication.domain.usecase.SyncPendingChangesUseCase
import com.example.myapplication.domain.usecase.ToggleLessonStatusUseCase
import com.example.myapplication.domain.util.Resource
import com.example.myapplication.ui.state.CourseDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val getCourseDetailsUseCase: GetCourseDetailsUseCase,
    private val toggleLessonStatusUseCase: ToggleLessonStatusUseCase,
    private val syncPendingChangesUseCase: SyncPendingChangesUseCase,
    private val networkMonitor: NetworkMonitor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val courseId: String = checkNotNull(savedStateHandle["courseId"])

    private val _isSyncing = MutableStateFlow(false)
    private val _syncMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CourseDetailUiState> = combine(
        getCourseDetailsUseCase(courseId),
        networkMonitor.isOnline,
        _isSyncing,
        _syncMessage
    ) { courseWithLessons: CourseWithLessons?, isOnline: Boolean, isSyncing: Boolean, message: String? ->
        if (courseWithLessons == null) {
            CourseDetailUiState.Loading
        } else {
            CourseDetailUiState.Success(
                courseWithLessons = courseWithLessons,
                isSyncing = isSyncing,
                isOnline = isOnline,
                syncMessage = message
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = CourseDetailUiState.Loading
    )

    fun toggleLesson(lessonId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            _isSyncing.value = true
            val newStatus = !currentStatus
            when (val result = toggleLessonStatusUseCase(courseId, lessonId, newStatus)) {
                is Resource.Success -> {
                    if (networkMonitor.isConnected()) {
                        _syncMessage.value = "Updated & synced to server"
                    } else {
                        _syncMessage.value = "Saved offline. Sync scheduled when online."
                    }
                }
                is Resource.Error -> {
                    _syncMessage.value = result.message
                }
                is Resource.Loading -> Unit
            }
            _isSyncing.value = false
        }
    }

    fun retrySync() {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val result = syncPendingChangesUseCase()) {
                is Resource.Success -> {
                    _syncMessage.value = "Sync complete"
                }
                is Resource.Error -> {
                    _syncMessage.value = "Sync failed. Will retry automatically."
                }
                is Resource.Loading -> Unit
            }
            _isSyncing.value = false
        }
    }

    fun dismissMessage() {
        _syncMessage.value = null
    }
}