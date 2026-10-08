package com.example.myapplication.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.converter.SyncStatusConverter
import com.example.myapplication.data.local.dao.CourseDao
import com.example.myapplication.data.local.dao.UserSessionDao
import com.example.myapplication.data.local.entity.CourseEntity
import com.example.myapplication.data.local.entity.LessonEntity
import com.example.myapplication.data.local.entity.UserSessionEntity

@Database(
    entities = [CourseEntity::class, LessonEntity::class, UserSessionEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun userSessionDao(): UserSessionDao
}
