package com.vulkanalia.aranews.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vulkanalia.aranews.api.AragonOpenDataApi
import com.vulkanalia.aranews.data.AppDatabase
import com.vulkanalia.aranews.data.NewsRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RefreshDataWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://opendata.aragon.es/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(AragonOpenDataApi::class.java)
        val repository = NewsRepository(database.newsDao(), api)

        return try {
            repository.refreshNews()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
