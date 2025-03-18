package com.okta.senov.model

data class BookData(
    val id: String,
    val title: String,
    val image: String,
    val authorName: String = "",
    val category: String = "",
    val description: String = ""
)
