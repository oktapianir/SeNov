package com.okta.senov.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "authors")
data class Author(
    val name: String,
//    val imageResId: Int
    val imageAuthor: String,
    val biography: String
): Parcelable
