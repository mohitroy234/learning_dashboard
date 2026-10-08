package com.example.myapplication.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class UserSessionEntity(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null
)
