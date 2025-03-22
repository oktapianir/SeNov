package com.okta.senov.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "authors")
data class Author(
    val id : String ="",
    val nameAuthor: String ="",
//    val imageResId: Int
    val imageUrl: String ="",
    val biography: String =""
): Parcelable
