package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.CourseRepository
import com.example.myapplication.domain.util.Resource
import javax.inject.Inject

class ToggleLessonStatusUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(
        courseId: String,
        lessonId: String,
        isCompleted: Boolean
    ): Resource<Unit> {
        return repository.updateLessonStatus(courseId, lessonId, isCompleted)
    }
}
