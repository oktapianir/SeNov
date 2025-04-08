package com.okta.senov.model

import com.google.firebase.Timestamp

data class Rating(
    val id: String = "",
    val bookId: String = "",
    val userId: String = "",
    val rating: Float = 0f,
    val review: String = "",
    val recommended: Boolean = false,
    val timestamp: Timestamp = Timestamp.now(),
    val userEmail: String = ""
)