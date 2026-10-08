package com.example.myapplication.ui.viewmodel.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.network.NetworkMonitor
import com.example.myapplication.data.sync.SyncScheduler
import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.usecase.GetCoursesUseCase
import com.example.myapplication.domain.usecase.RefreshCoursesUseCase
import com.example.myapplication.domain.usecase.SyncPendingChangesUseCase
import com.example.myapplication.domain.util.Resource
import com.example.myapplication.ui.state.CourseListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val getCoursesUseCase: GetCoursesUseCase,
    private val refreshCoursesUseCase: RefreshCoursesUseCase,
    private val syncPendingChangesUseCase: SyncPendingChangesUseCase,
    private val networkMonitor: NetworkMonitor,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CourseListUiState> = combine(
        getCoursesUseCase(),
        networkMonitor.isOnline,
        _isSyncing,
        _userMessage
    ) { courses: List<Course>, isOnline: Boolean, isSyncing: Boolean, message: String? ->
        if (courses.isEmpty() && isSyncing) {
            CourseListUiState.Loading
        } else {
            CourseListUiState.Success(
                courses = courses,
                isSyncing = isSyncing,
                isOnline = isOnline,
                userMessage = message
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.WhileSubscribed(5000),
        initialValue = CourseListUiState.Loading
    )

    init {
        // Schedule periodic sync via WorkManager in background
        syncScheduler.schedulePeriodicSync()

        // 1. Sync local from server on app launch
        refreshCourses()

        // Listen for network coming back online to flush pending changes
        observeNetworkChanges()
    }

    private fun observeNetworkChanges() {
        viewModelScope.launch {
            networkMonitor.isOnline
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        // Network became available -> sync pending changes
                        syncPendingChanges()
                    }
                }
        }
    }

    fun refreshCourses() {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val result = refreshCoursesUseCase()) {
                is Resource.Success -> {
                    _userMessage.value = "Synced with server"
                }
                is Resource.Error -> {
                    _userMessage.value = result.message
                }
                is Resource.Loading -> Unit
            }
            _isSyncing.value = false
        }
    }

    fun syncPendingChanges() {
        viewModelScope.launch {
            _isSyncing.value = true
            when (val result = syncPendingChangesUseCase()) {
                is Resource.Success -> {
                    _userMessage.value = "Offline changes synced to server"
                }
                is Resource.Error -> {
                    // Handled gracefully, Worker will retry
                }
                is Resource.Loading -> Unit
            }
            _isSyncing.value = false
        }
    }

    fun dismissMessage() {
        _userMessage.value = null
    }
}