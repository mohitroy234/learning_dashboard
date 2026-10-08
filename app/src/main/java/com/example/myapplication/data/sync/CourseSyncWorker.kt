package com.example.myapplication.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.domain.repository.CourseRepository
import com.example.myapplication.domain.util.Resource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class CourseSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CourseRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return when (val result = repository.syncPendingChanges()) {
            is Resource.Success -> Result.success()
            is Resource.Error -> {
                // If under retry limit (3 retries), return retry with exponential backoff
                if (runAttemptCount < 3) {
                    Result.retry()
                } else {
                    Result.failure()
                }
            }
            is Resource.Loading -> Result.retry()
        }
    }
}
