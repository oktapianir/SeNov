package com.okta.senov.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.okta.senov.model.Book

@Database(entities = [Book::class], version = 2, exportSchema = false)
abstract class BookDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}