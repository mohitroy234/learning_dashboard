package com.example.myapplication.data.remote.model

import com.google.gson.annotations.SerializedName

data class CourseDto(
    @SerializedName("id") val id: String,
    @SerializedName("courseId") val courseId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("instructorName") val instructorName: String,
    @SerializedName("numberOfLesson") val numberOfLesson: Int,
    @SerializedName("progress") val progress: Int,
    @SerializedName("createdAt") val createdAt: String
)

data class CourseUpdateDto(
    @SerializedName("progress") val progress: Int
)