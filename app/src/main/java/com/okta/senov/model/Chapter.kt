package com.okta.senov.model

import java.io.Serializable

data class Chapter(
    val number: Int = 0,
    val title: String = "",
    val content: String = ""
) : Serializable