package com.d104.pnt.data.remote.model.response

data class GameNewsResponse(
    val content: String,
    val createdAt: String,
    val gameId: Int,
    val newsId: Int,
    val title: String
)