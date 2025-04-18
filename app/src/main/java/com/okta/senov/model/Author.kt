package com.okta.senov.model

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(tableName = "authors")
data class Author(
    val idAuthor : String ="",
    val nameAuthor: String ="",
    val socialMedia: String ="",
    val imageUrl: String ="",
    val bioAuthor: String =""
): Parcelable
