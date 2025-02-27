package com.okta.senov.model

data class AuthorResponse(
    val status: String,
    val total_results: Int,
    val total_pages: Int,
    val current_page: Int,
    val authors: List<Author>
)
