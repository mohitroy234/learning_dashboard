package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.UserSessionDao
import com.example.myapplication.data.local.entity.UserSessionEntity
import com.example.myapplication.data.remote.api.AuthAPI
import com.example.myapplication.data.remote.model.LoginResponse
import com.example.myapplication.data.remote.model.RegisterRequest
import com.example.myapplication.data.remote.model.RegisterResponse
import com.example.myapplication.domain.repository.AuthRepository
import com.example.myapplication.domain.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authAPI: AuthAPI,
    private val userSessionDao: UserSessionDao
) : AuthRepository {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    override suspend fun login(email: String, password: String): Resource<LoginResponse> =
        withContext(ioDispatcher) {
            try {
                val response = authAPI.login(email)
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val user = response.body()!!.first()
                    // Save session locally
                    userSessionDao.saveSession(
                        UserSessionEntity(
                            isLoggedIn = true,
                            userName = user.name,
                            userEmail = user.email
                        )
                    )
                    Resource.Success(user)
                } else if (response.code() == 404) {
                    Resource.Error("No account found with this email")
                } else if (response.isSuccessful && response.body().isNullOrEmpty()) {
                    Resource.Error("No account found with this email")
                } else {
                    Resource.Error("Login failed: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                Resource.Error("Login failed: ${e.localizedMessage ?: "Network error"}", e)
            }
        }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): Resource<RegisterResponse> = withContext(ioDispatcher) {
        try {
            val response = authAPI.register(RegisterRequest(name, email, password))
            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!
                // Save session locally after successful registration
                userSessionDao.saveSession(
                    UserSessionEntity(
                        isLoggedIn = true,
                        userName = user.name,
                        userEmail = user.email
                    )
                )
                Resource.Success(user)
            } else {
                Resource.Error("Registration failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(
                "Registration failed: ${e.localizedMessage ?: "Network error"}",
                e
            )
        }
    }

    override suspend fun logout() = withContext(ioDispatcher) {
        userSessionDao.clearSession()
    }
}
