package com.example.myapplication.domain.repository

import com.example.myapplication.data.remote.model.LoginResponse
import com.example.myapplication.data.remote.model.RegisterResponse
import com.example.myapplication.domain.util.Resource

interface AuthRepository {

    suspend fun login(email: String, password: String): Resource<LoginResponse>

    suspend fun register(name: String, email: String, password: String): Resource<RegisterResponse>

    suspend fun logout()
}
