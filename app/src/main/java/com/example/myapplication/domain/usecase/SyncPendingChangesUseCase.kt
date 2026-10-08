package com.example.myapplication.domain.usecase

import com.example.myapplication.domain.repository.CourseRepository
import com.example.myapplication.domain.util.Resource
import javax.inject.Inject

class SyncPendingChangesUseCase @Inject constructor(
    private val repository: CourseRepository
) {
    suspend operator fun invoke(): Resource<Unit> = repository.syncPendingChanges()
}
