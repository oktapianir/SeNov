    package com.okta.senov.model

    data class BookResponse(
        val available: Int,
        val number: Int,
        val offset: Int,
        val books: List<List<BookData>>
    )

    data class BookData(
        val id: Int,
        val title: String,
        val image: String

    )
