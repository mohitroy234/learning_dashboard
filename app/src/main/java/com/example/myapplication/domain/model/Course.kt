package com.example.myapplication.domain.model

data class Course(
    val id: String,
    val courseId: Int,
    val name: String,
    val instructorName: String,
    val numberOfLesson: Int,
    val progress: Int,
    val createdAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
