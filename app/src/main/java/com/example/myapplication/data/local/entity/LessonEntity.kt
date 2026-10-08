package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.SyncStatus

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["courseId"])]
)
data class LessonEntity(
    @PrimaryKey
    val id: String,
    val courseId: String,
    val title: String,
    val isCompleted: Boolean,
    val orderIndex: Int,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
