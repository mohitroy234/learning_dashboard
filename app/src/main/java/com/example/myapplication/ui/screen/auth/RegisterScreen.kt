package com.example.myapplication.ui.screen.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.viewmodel.auth.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    registerState: AuthViewModel.AuthUiState = AuthViewModel.AuthUiState.Idle
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true
        nameError = when {
            name.isBlank() -> { valid = false; "Name is required" }
            name.length < 2 -> { valid = false; "Name must be at least 2 characters" }
            else -> null
        }
        emailError = when {
            email.isBlank() -> { valid = false; "Email is required" }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> { valid = false; "Enter a valid email" }
            else -> null
        }
        passwordError = when {
            password.isBlank() -> { valid = false; "Password is required" }
            password.length < 6 -> { valid = false; "Password must be at least 6 characters" }
            else -> null
        }
        return valid
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (nameError != null) nameError = null
            },
            label = { Text("Name") },
            singleLine = true,
            isError = nameError != null,
            supportingText = nameError?.let { error -> { Text(error) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthViewModel.AuthUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) emailError = null
            },
            label = { Text("Email") },
            singleLine = true,
            isError = emailError != null,
            supportingText = emailError?.let { error -> { Text(error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthViewModel.AuthUiState.Loading
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null
            },
            label = { Text("Password") },
            singleLine = true,
            isError = passwordError != null,
            supportingText = passwordError?.let { error -> { Text(error) } },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthViewModel.AuthUiState.Loading
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (validate()) {
                    onRegisterClick(name, email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = registerState !is AuthViewModel.AuthUiState.Loading
        ) {
            if (registerState is AuthViewModel.AuthUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registering...")
            } else {
                Text("Register")
            }
        }

        if (registerState is AuthViewModel.AuthUiState.Error) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = registerState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Login",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
    }
}
