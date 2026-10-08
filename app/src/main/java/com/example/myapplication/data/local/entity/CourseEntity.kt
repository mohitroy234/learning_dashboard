package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.SyncStatus

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    val id: String,
    val courseId: Int,
    val name: String,
    val instructorName: String,
    val numberOfLesson: Int,
    val progress: Int,
    val createdAt: String,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
