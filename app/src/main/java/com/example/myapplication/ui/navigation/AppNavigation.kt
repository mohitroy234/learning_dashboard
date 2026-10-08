package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.viewmodel.auth.AuthViewModel

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val loginState by authViewModel.loginState.collectAsState()
    val registerState by authViewModel.registerState.collectAsState()

    if (isLoggedIn) {
        CourseNavigation(
            onLogout = {
                authViewModel.logout()
            }
        )
    } else {
        AuthNavigation(
            loginState = loginState,
            registerState = registerState,
            onLoginClick = { email, password ->
                authViewModel.login(email, password)
            },
            onRegisterClick = { name, email, password ->
                authViewModel.register(name, email, password)
            }
        )
    }
}