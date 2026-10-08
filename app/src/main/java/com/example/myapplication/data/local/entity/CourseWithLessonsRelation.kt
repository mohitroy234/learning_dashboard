package com.example.myapplication.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CourseWithLessonsRelation(
    @Embedded
    val course: CourseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "courseId"
    )
    val lessons: List<LessonEntity>
)
