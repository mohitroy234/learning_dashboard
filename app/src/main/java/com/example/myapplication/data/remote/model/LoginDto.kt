package com.example.myapplication.data.remote.model

import androidx.annotation.Keep

data class LoginRequest(
    val email: String,
    val password: String
)

@Keep
data class LoginResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)