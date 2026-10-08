package com.example.myapplication.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Course : Screen("course")
    data object CourseDetail : Screen("courseDetail/{courseId}") {
        fun createRoute(courseId: String): String = "courseDetail/$courseId"
    }
}