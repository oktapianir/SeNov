package com.okta.senov.model

import java.io.Serializable

data class BookContent(
    val bookId: String = "",
    val title: String = "",
    val chapters: List<Chapter> = emptyList()
) : Serializable