package com.example.myapplication.domain.model

data class Lesson(
    val id: String,
    val courseId: String,
    val title: String,
    val isCompleted: Boolean,
    val orderIndex: Int,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
