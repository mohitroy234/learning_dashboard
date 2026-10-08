package com.example.myapplication.data.remote.api

import com.example.myapplication.data.remote.model.CourseDto
import com.example.myapplication.data.remote.model.CourseUpdateDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface CourseAPI {

    @GET("course")
    suspend fun getCourses(): Response<List<CourseDto>>

    @GET("course/{id}")
    suspend fun getCourseById(
        @Path("id") id: String
    ): Response<CourseDto>

    @PUT("course/{id}")
    suspend fun updateCourse(
        @Path("id") id: String,
        @Body body: CourseUpdateDto
    ): Response<CourseDto>
}