package com.okta.senov.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: Int,
    val title: String,
    val author: String,
    val genre: String,
    val rating: Float,
    val price: String,
    val synopsis: String,
    val coverResourceId: Int
) : Parcelable