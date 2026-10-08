package com.example.myapplication.ui.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.UserSessionDao
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userSessionDao: UserSessionDao
) : ViewModel() {

    sealed interface AuthUiState {
        data object Idle : AuthUiState
        data object Loading : AuthUiState
        data object Success : AuthUiState
        data class Error(val message: String) : AuthUiState
    }

    val isLoggedIn: StateFlow<Boolean> = userSessionDao.getSession()
        .map { session -> session?.isLoggedIn == true }
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), false)

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState

    private val _registerState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState> = _registerState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is Resource.Success -> {
                    _loginState.value = AuthUiState.Success
                }
                is Resource.Error -> {
                    _loginState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = AuthUiState.Loading
            when (val result = authRepository.register(name, email, password)) {
                is Resource.Success -> {
                    _registerState.value = AuthUiState.Success
                }
                is Resource.Error -> {
                    _registerState.value = AuthUiState.Error(result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = AuthUiState.Idle
            _registerState.value = AuthUiState.Idle
        }
    }

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }
}