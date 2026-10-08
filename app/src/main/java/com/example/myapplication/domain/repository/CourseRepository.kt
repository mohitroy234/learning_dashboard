package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.model.CourseWithLessons
import com.example.myapplication.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface CourseRepository {
    /**
     * Observes the list of courses from the local database (Single Source of Truth).
     */
    fun getCoursesStream(): Flow<List<Course>>

    /**
     * Observes a specific course along with its lessons from the local database.
     */
    fun getCourseWithLessonsStream(courseId: String): Flow<CourseWithLessons?>

    /**
     * Syncs local database from remote API on app launch or manual pull-to-refresh.
     */
    suspend fun refreshCoursesFromServer(): Resource<Unit>

    /**
     * Updates lesson completion status locally first (optimistic offline-first update),
     * recalculates course progress, and triggers sync to server with retry / scheduling logic.
     */
    suspend fun updateLessonStatus(courseId: String, lessonId: String, isCompleted: Boolean): Resource<Unit>

    /**
     * Syncs any locally pending changes to the server. Used by Worker and manual retry.
     */
    suspend fun syncPendingChanges(): Resource<Unit>
}
