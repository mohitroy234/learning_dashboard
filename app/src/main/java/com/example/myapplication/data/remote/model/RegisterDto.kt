package com.example.myapplication.data.remote.model

import androidx.annotation.Keep

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

@Keep
data class RegisterResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: String
)