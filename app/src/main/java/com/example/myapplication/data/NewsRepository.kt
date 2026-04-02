package com.example.myapplication.data

import com.example.myapplication.api.AragonOpenDataApi
import kotlinx.coroutines.flow.Flow

class NewsRepository(
    private val newsDao: NewsDao,
    private val api: AragonOpenDataApi
) {
    val allNews: Flow<List<NewsItem>> = newsDao.getAllNews()

    fun searchNews(query: String): Flow<List<NewsItem>> {
        return newsDao.searchNews(query)
    }

    suspend fun refreshNews() {
        try {
            val newsFromApi = api.getNews()
            newsDao.refreshNews(newsFromApi)
        } catch (e: Exception) {
            // Handle error (e.g., logging)
            throw e
        }
    }
}
