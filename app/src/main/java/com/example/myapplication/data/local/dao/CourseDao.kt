package com.example.myapplication.data.local.dao

import androidx.room.*
import com.example.myapplication.data.local.entity.CourseEntity
import com.example.myapplication.data.local.entity.CourseWithLessonsRelation
import com.example.myapplication.data.local.entity.LessonEntity
import com.example.myapplication.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Query("SELECT * FROM courses ORDER BY CAST(id AS INTEGER) ASC")
    fun observeCourses(): Flow<List<CourseEntity>>

    @Transaction
    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    fun observeCourseWithLessons(courseId: String): Flow<CourseWithLessonsRelation?>

    @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseEntity?

    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    suspend fun getLessonsForCourse(courseId: String): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourses(courses: List<CourseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourse(course: CourseEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLessonsIfNotExist(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessons(lessons: List<LessonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLesson(lesson: LessonEntity)

    @Query("UPDATE lessons SET isCompleted = :isCompleted, syncStatus = :syncStatus WHERE id = :lessonId")
    suspend fun updateLessonStatus(lessonId: String, isCompleted: Boolean, syncStatus: SyncStatus)

    @Query("UPDATE courses SET progress = :progress, syncStatus = :syncStatus WHERE id = :courseId")
    suspend fun updateCourseProgress(courseId: String, progress: Int, syncStatus: SyncStatus)

    @Query("SELECT COUNT(*) FROM lessons WHERE courseId = :courseId")
    suspend fun getLessonsCountForCourse(courseId: String): Int

    @Query("SELECT * FROM courses WHERE syncStatus = :syncStatus")
    suspend fun getCoursesBySyncStatus(syncStatus: SyncStatus): List<CourseEntity>

    @Query("SELECT * FROM lessons WHERE syncStatus = :syncStatus")
    suspend fun getLessonsBySyncStatus(syncStatus: SyncStatus): List<LessonEntity>

    @Query("UPDATE courses SET syncStatus = 'SYNCED' WHERE id = :courseId")
    suspend fun markCourseSynced(courseId: String)

    @Query("UPDATE lessons SET syncStatus = 'SYNCED' WHERE id = :lessonId")
    suspend fun markLessonSynced(lessonId: String)
}
