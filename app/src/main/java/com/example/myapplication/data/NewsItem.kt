package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "news_items")
data class NewsItem(
    @PrimaryKey
    @SerializedName("WNOV_ID")
    val id: Long,
    @SerializedName("F_FINVIGENCIA")
    val fFinVigencia: String?,
    @SerializedName("F_GRABACION")
    val fGrabacion: String?,
    @SerializedName("F_VIGENCIA")
    val fVigencia: String?,
    @SerializedName("NOTICIA")
    val noticia: String?,
    @SerializedName("TITULO")
    val titulo: String?,
    @SerializedName("URL")
    val url: String?
)
