package com.okta.senov.model

import java.util.Date

data class SupportRequest(
    val id_support_request: String = "",
    val name: String = "",
    val email: String = "",
    val description: String = "",
    val timestamp: Date = Date(),
    val status: String = "Pending"
)