package com.okta.senov.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Book(
    val title: String,
    val author: String,
    val genre: String,
    val rating: Float,
    val price: String,
   @DrawableRes val coverResourceId: Int
) : Parcelable
