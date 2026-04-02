package com.example.myapplication.api

import com.example.myapplication.data.NewsItem
import retrofit2.http.GET
import retrofit2.http.Query

interface AragonOpenDataApi {
    @GET("GA_OD_Core/download")
    suspend fun getNews(
        @Query("resource_id") resourceId: Int = 25,
        @Query("formato") formato: String = "json"
    ): List<NewsItem>
}
