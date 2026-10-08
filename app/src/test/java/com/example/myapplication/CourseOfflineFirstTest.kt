package com.example.myapplication

import com.example.myapplication.data.local.entity.CourseEntity
import com.example.myapplication.data.local.entity.CourseWithLessonsRelation
import com.example.myapplication.data.local.entity.LessonEntity
import com.example.myapplication.data.mapper.toDomain
import com.example.myapplication.data.mapper.toEntity
import com.example.myapplication.data.remote.model.CourseDto
import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.model.Lesson
import com.example.myapplication.domain.model.SyncStatus
import com.example.myapplication.domain.util.Resource
import com.example.myapplication.ui.state.CourseDetailUiState
import com.example.myapplication.ui.state.CourseListUiState
import org.junit.Assert.*
import org.junit.Test

class CourseOfflineFirstTest {

    @Test
    fun resource_sealedClass_stateIntegrity() {
        val loading: Resource<String> = Resource.Loading
        val success: Resource<String> = Resource.Success("Course Data")
        val error: Resource<String> = Resource.Error("Network Failed")

        assertTrue(loading is Resource.Loading)
        assertTrue(success is Resource.Success && success.data == "Course Data")
        assertTrue(error is Resource.Error && error.message == "Network Failed")
    }

    @Test
    fun uiState_sealedClass_stateIntegrity() {
        val course = Course(
            id = "1",
            courseId = 50,
            name = "Kotlin Clean Architecture",
            instructorName = "Alex",
            numberOfLesson = 4,
            progress = 50,
            createdAt = "2026-10-06T16:35:00.151Z",
            syncStatus = SyncStatus.SYNCED
        )

        val listSuccess = CourseListUiState.Success(
            courses = listOf(course),
            isSyncing = false,
            isOnline = true
        )
        assertEquals(1, listSuccess.courses.size)
        assertTrue(listSuccess.isOnline)
        assertFalse(listSuccess.isSyncing)
    }

    @Test
    fun mapper_dtoToEntityAndDomain() {
        val dto = CourseDto(
            id = "10",
            courseId = 99,
            name = "Advanced Coroutines",
            instructorName = "Jane Doe",
            numberOfLesson = 10,
            progress = 20,
            createdAt = "2026-10-07T00:00:00.000Z"
        )

        val entity = dto.toEntity(SyncStatus.SYNCED)
        assertEquals("10", entity.id)
        assertEquals(SyncStatus.SYNCED, entity.syncStatus)

        val domain = entity.toDomain()
        assertEquals(dto.name, domain.name)
        assertEquals(dto.progress, domain.progress)
    }

    @Test
    fun progressCalculation_whenLessonToggled() {
        val lessons = listOf(
            Lesson(id = "l1", courseId = "1", title = "Introduction", isCompleted = true, orderIndex = 0),
            Lesson(id = "l2", courseId = "1", title = "Variables", isCompleted = true, orderIndex = 1),
            Lesson(id = "l3", courseId = "1", title = "Functions", isCompleted = false, orderIndex = 2),
            Lesson(id = "l4", courseId = "1", title = "OOP", isCompleted = false, orderIndex = 3)
        )

        val completedCount = lessons.count { it.isCompleted }
        val progress = ((completedCount.toDouble() / lessons.size.toDouble()) * 100).toInt()
        assertEquals(50, progress)

        // Toggle third lesson to completed
        val updatedLessons = lessons.map {
            if (it.id == "l3") it.copy(isCompleted = true, syncStatus = SyncStatus.PENDING_SYNC) else it
        }
        val updatedCompletedCount = updatedLessons.count { it.isCompleted }
        val updatedProgress = ((updatedCompletedCount.toDouble() / updatedLessons.size.toDouble()) * 100).toInt()
        assertEquals(75, updatedProgress)
        assertEquals(SyncStatus.PENDING_SYNC, updatedLessons.first { it.id == "l3" }.syncStatus)
    }
}
