package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_items ORDER BY fGrabacion DESC")
    fun getAllNews(): Flow<List<NewsItem>>

    @Query("SELECT * FROM news_items WHERE titulo LIKE '%' || :query || '%' OR noticia LIKE '%' || :query || '%' ORDER BY fGrabacion DESC")
    fun searchNews(query: String): Flow<List<NewsItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(news: List<NewsItem>)

    @Query("DELETE FROM news_items")
    suspend fun deleteAll()

    @Transaction
    suspend fun refreshNews(news: List<NewsItem>) {
        deleteAll()
        insertAll(news)
    }
}
