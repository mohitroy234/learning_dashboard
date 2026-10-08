package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCoursesUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    operator fun invoke(): Flow<List<Course>> = repository.getCoursesStream()
}
