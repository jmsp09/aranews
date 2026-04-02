package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.api.AragonOpenDataApi
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.NewsItem
import com.example.myapplication.data.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NewsRepository
    private val prefs = application.getSharedPreferences("news_prefs", Context.MODE_PRIVATE)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdate = MutableStateFlow(prefs.getString("last_update", null))
    val lastUpdate: StateFlow<String?> = _lastUpdate.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://opendata.aragon.es/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(AragonOpenDataApi::class.java)
        repository = NewsRepository(database.newsDao(), api)
    }

    val newsList: StateFlow<List<NewsItem>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isEmpty()) {
                repository.allNews
            } else {
                repository.searchNews(query)
            }
        }.let { flow ->
            val stateFlow = MutableStateFlow<List<NewsItem>>(emptyList())
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
            stateFlow.asStateFlow()
        }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                repository.refreshNews()
                val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                val dateStr = "Hoy a las $now"
                _lastUpdate.value = dateStr
                prefs.edit().putString("last_update", dateStr).apply()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
