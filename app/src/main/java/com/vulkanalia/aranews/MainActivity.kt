package com.vulkanalia.aranews

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.work.*
import com.vulkanalia.aranews.ui.NewsScreen
import com.vulkanalia.aranews.ui.NewsViewModel
import com.vulkanalia.aranews.worker.RefreshDataWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: NewsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        scheduleDailyUpdate()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NewsScreen(viewModel = viewModel)
                }
            }
        }
        
        // Initial refresh if database is empty
        viewModel.refreshNews()
    }

    private fun scheduleDailyUpdate() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<RefreshDataWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "RefreshNewsWork",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )
    }
}
