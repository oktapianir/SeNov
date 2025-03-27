package com.okta.senov.model

import java.util.Date

data class SupportRequest(
    val name: String = "",
    val email: String = "",
    val category: String = "",
    val description: String = "",
    val timestamp: Date = Date(),
    val status: String = "Pending"
)