package com.example.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screen.course.CourseDashboardScreen
import com.example.myapplication.ui.screen.course.CourseDetailScreen

@Composable
fun CourseNavigation(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Course.route
    ) {
        composable(Screen.Course.route) {
            CourseDashboardScreen(
                onCourseClick = { course ->
                    navController.navigate(Screen.CourseDetail.createRoute(course.id))
                }
            )
        }

        composable(
            route = Screen.CourseDetail.route,
            arguments = listOf(
                navArgument("courseId") {
                    type = NavType.StringType
                }
            )
        ) {
            CourseDetailScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
