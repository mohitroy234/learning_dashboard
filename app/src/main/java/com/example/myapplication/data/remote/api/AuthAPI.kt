package com.example.myapplication.data.remote.api

import com.example.myapplication.data.remote.model.LoginResponse
import com.example.myapplication.data.remote.model.RegisterRequest
import com.example.myapplication.data.remote.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthAPI {

    @GET("user/{email}")
    suspend fun login(
        @Path("email") email: String
    ): Response<List<LoginResponse>>

    @POST("user")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<RegisterResponse>

}