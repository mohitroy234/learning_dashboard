package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.CourseWithLessons
import com.example.myapplication.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCourseDetailsUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    operator fun invoke(courseId: String): Flow<CourseWithLessons?> =
        repository.getCourseWithLessonsStream(courseId)
}
