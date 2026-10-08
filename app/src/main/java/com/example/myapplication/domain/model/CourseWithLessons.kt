package com.example.myapplication.domain.model

data class CourseWithLessons(
    val course: Course,
    val lessons: List<Lesson>
)
