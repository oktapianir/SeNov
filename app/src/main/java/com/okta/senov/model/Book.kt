package com.okta.senov.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "Books")
data class Book(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val authorName: String = "",
    val category: String = "",
    val description: String = "",
      val image: String = ""
    ) : Parcelable

