package com.okta.senov.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: Int,
    val title: String,
//    val author: String,
//    val genre: String,
//    val rating: Float,
//    val price: Float,
    val synopsis: String? = null,
      val coverResourceId: String
) : Parcelable