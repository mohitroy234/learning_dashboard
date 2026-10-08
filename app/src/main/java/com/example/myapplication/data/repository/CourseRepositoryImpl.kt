package com.example.myapplication.data.repository

import com.example.myapplication.data.local.dao.CourseDao
import com.example.myapplication.data.local.entity.LessonEntity
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.network.NetworkMonitor
import com.example.myapplication.data.remote.api.CourseAPI
import com.example.myapplication.data.remote.model.CourseUpdateDto
import com.example.myapplication.data.sync.SyncScheduler
import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.model.CourseWithLessons
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.domain.repository.CourseRepository
import com.example.myapplication.domain.util.Resource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val courseAPI: CourseAPI,
    private val networkMonitor: NetworkMonitor,
    private val syncScheduler: SyncScheduler
) : CourseRepository {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO


    override fun getCoursesStream(): Flow<List<Course>> {
        return courseDao.observeCourses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCourseWithLessonsStream(courseId: String): Flow<CourseWithLessons?> {
        return courseDao.observeCourseWithLessons(courseId).map { relation ->
            relation?.toDomain()
        }
    }

    override suspend fun refreshCoursesFromServer(): Resource<Unit> = withContext(ioDispatcher) {
        if (!networkMonitor.isConnected()) {
            return@withContext Resource.Error("Offline: Loaded from local database")
        }

        try {
            val response = courseAPI.getCourses()
            if (response.isSuccessful && response.body() != null) {
                val dtoList = response.body()!!
                for (dto in dtoList) {
                    val existing = courseDao.getCourseById(dto.id)
                    // If course locally has pending changes, do not overwrite until synced
                    if (existing != null && existing.syncStatus == SyncStatus.PENDING_SYNC) {
                        continue
                    }

                    val entity = dto.toEntity(SyncStatus.SYNCED)
                    courseDao.upsertCourse(entity)

                    // Check if lessons already exist for this course
                    val lessonCount = courseDao.getLessonsCountForCourse(dto.id)
                    if (lessonCount == 0) {
                        val initialLessons = generateDefaultLessons(
                            courseDbId = dto.id,
                            courseIdNumber = dto.courseId,
                            courseName = dto.name,
                            totalLessonsCount = dto.numberOfLesson,
                            currentProgress = dto.progress
                        )
                        courseDao.insertLessonsIfNotExist(initialLessons)
                    }
                }
                Resource.Success(Unit)
            } else {
                Resource.Error("Server error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to fetch courses: ${e.localizedMessage ?: "Unknown error"}", e)
        }
    }

    override suspend fun updateLessonStatus(
        courseId: String,
        lessonId: String,
        isCompleted: Boolean
    ): Resource<Unit> = withContext(ioDispatcher) {
        // 1. Update local database immediately (Offline-First optimistic update)
        courseDao.updateLessonStatus(lessonId, isCompleted, SyncStatus.PENDING_SYNC)

        // 2. Recompute course progress from local lessons
        val lessons = courseDao.getLessonsForCourse(courseId)
        val completedCount = lessons.count { it.isCompleted }
        val newProgress = if (lessons.isNotEmpty()) {
            ((completedCount.toDouble() / lessons.size.toDouble()) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
        courseDao.updateCourseProgress(courseId, newProgress, SyncStatus.PENDING_SYNC)

        // 3. If network is available, try immediate sync with 3 retries
        if (networkMonitor.isConnected()) {
            val syncSuccess = attemptSyncWithRetries(courseId, newProgress, lessonId)
            if (syncSuccess) {
                return@withContext Resource.Success(Unit)
            } else {
                // If 3 retries failed, schedule for later via WorkManager
                syncScheduler.scheduleSync()
                return@withContext Resource.Error("Sync failed after 3 retries. Saved locally & scheduled for later.")
            }
        } else {
            // Offline: schedule for later via WorkManager
            syncScheduler.scheduleSync()
            return@withContext Resource.Success(Unit)
        }
    }

    private suspend fun attemptSyncWithRetries(
        courseId: String,
        newProgress: Int,
        lessonId: String
    ): Boolean {
        val maxRetries = 3
        var attempt = 0
        while (attempt < maxRetries) {
            attempt++
            try {
                val response = courseAPI.updateCourse(
                    id = courseId,
                    body = CourseUpdateDto(progress = newProgress)
                )
                if (response.isSuccessful) {
                    courseDao.markCourseSynced(courseId)
                    courseDao.markLessonSynced(lessonId)
                    return true
                }
            } catch (e: Exception) {
                // Network error during retry
            }
            if (attempt < maxRetries) {
                delay(attempt * 500L) // Linear / exponential backoff
            }
        }
        return false
    }

    override suspend fun syncPendingChanges(): Resource<Unit> = withContext(ioDispatcher) {
        if (!networkMonitor.isConnected()) {
            return@withContext Resource.Error("Network unavailable for sync")
        }

        val pendingCourses = courseDao.getCoursesBySyncStatus(SyncStatus.PENDING_SYNC)
        var allSucceeded = true

        for (course in pendingCourses) {
            val lessons = courseDao.getLessonsForCourse(course.id)
            val completedCount = lessons.count { it.isCompleted }
            val progress = if (lessons.isNotEmpty()) {
                ((completedCount.toDouble() / lessons.size.toDouble()) * 100).toInt().coerceIn(0, 100)
            } else {
                course.progress
            }

            var syncSuccess = false
            var attempt = 0
            while (attempt < 3 && !syncSuccess) {
                attempt++
                try {
                    val response = courseAPI.updateCourse(course.id, CourseUpdateDto(progress = progress))
                    if (response.isSuccessful) {
                        courseDao.markCourseSynced(course.id)
                        lessons.forEach { courseDao.markLessonSynced(it.id) }
                        syncSuccess = true
                    }
                } catch (e: Exception) {
                    // Retry
                }
                if (!syncSuccess && attempt < 3) {
                    delay(attempt * 500L)
                }
            }

            if (!syncSuccess) {
                allSucceeded = false
            }
        }

        if (allSucceeded) {
            Resource.Success(Unit)
        } else {
            Resource.Error("Some items failed to sync to server")
        }
    }

    /**
     * Generates a curated list of lessons tailored for each course ID, matching
     * the user's requirement:
     * - Introduction (✓ Completed)
     * - Variables & Data Types (✓ Completed)
     * - Functions (○ Pending)
     * - OOP (○ Pending)
     * etc.
     */
    private fun generateDefaultLessons(
        courseDbId: String,
        courseIdNumber: Int,
        courseName: String,
        totalLessonsCount: Int,
        currentProgress: Int
    ): List<LessonEntity> {
        val lessonTemplates = listOf(
            "Introduction",
            "Variables & Data Types",
            "Functions",
            "OOP",
            "Control Flow & Loops",
            "Collections & Data Structures",
            "Coroutines & Asynchronous Flow",
            "Clean Architecture Principles",
            "Room Database & Offline-First Storage",
            "WorkManager & Background Synchronization",
            "State Management & Sealed Classes",
            "Unit Testing & Integration Testing"
        )

        // Choose number of lessons to display (e.g., minimum 4 up to 8 for clear visibility)
        val lessonCount = totalLessonsCount.coerceIn(4, 8)
        val completedThreshold = (currentProgress * lessonCount) / 100

        return (0 until lessonCount).map { index ->
            val title = lessonTemplates.getOrElse(index) { "Module ${index + 1}: Advanced Topics" }
            val isCompleted = index < completedThreshold

            LessonEntity(
                id = "${courseDbId}_lesson_${index + 1}",
                courseId = courseDbId,
                title = title,
                isCompleted = isCompleted,
                orderIndex = index,
                syncStatus = SyncStatus.SYNCED
            )
        }
    }
}
