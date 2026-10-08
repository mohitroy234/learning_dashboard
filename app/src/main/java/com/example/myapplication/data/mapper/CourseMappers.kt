package com.example.myapplication.data.mapper

import com.example.myapplication.data.local.entity.CourseEntity
import com.example.myapplication.data.local.entity.CourseWithLessonsRelation
import com.example.myapplication.data.local.entity.LessonEntity
import com.example.myapplication.data.remote.model.CourseDto
import com.example.myapplication.domain.model.Course
import com.example.myapplication.domain.model.CourseWithLessons
import com.example.myapplication.domain.model.Lesson
import com.example.myapplication.domain.model.SyncStatus

fun CourseDto.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): CourseEntity {
    return CourseEntity(
        id = id,
        courseId = courseId,
        name = name,
        instructorName = instructorName,
        numberOfLesson = numberOfLesson,
        progress = progress,
        createdAt = createdAt,
        syncStatus = syncStatus
    )
}

fun CourseEntity.toDomain(): Course {
    return Course(
        id = id,
        courseId = courseId,
        name = name,
        instructorName = instructorName,
        numberOfLesson = numberOfLesson,
        progress = progress,
        createdAt = createdAt,
        syncStatus = syncStatus
    )
}

fun LessonEntity.toDomain(): Lesson {
    return Lesson(
        id = id,
        courseId = courseId,
        title = title,
        isCompleted = isCompleted,
        orderIndex = orderIndex,
        syncStatus = syncStatus
    )
}

fun CourseWithLessonsRelation.toDomain(): CourseWithLessons {
    return CourseWithLessons(
        course = course.toDomain(),
        lessons = lessons.sortedBy { it.orderIndex }.map { it.toDomain() }
    )
}

fun Course.toEntity(): CourseEntity {
    return CourseEntity(
        id = id,
        courseId = courseId,
        name = name,
        instructorName = instructorName,
        numberOfLesson = numberOfLesson,
        progress = progress,
        createdAt = createdAt,
        syncStatus = syncStatus
    )
}

fun Lesson.toEntity(): LessonEntity {
    return LessonEntity(
        id = id,
        courseId = courseId,
        title = title,
        isCompleted = isCompleted,
        orderIndex = orderIndex,
        syncStatus = syncStatus
    )
}
