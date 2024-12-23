package com.okta.senov.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "search")
data class Search(
    @PrimaryKey val id: Int,
    @SerializedName("available")
    val available: Int,
    @SerializedName("books")
    val books: List<List<Book>>,
    @SerializedName("number")
    val number: Int,
    @SerializedName("offset")
    val offset: Int
)


