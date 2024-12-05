package com.okta.senov.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.okta.senov.model.Book

@Dao
interface BookDao {

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Update
    suspend fun update(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books: List<Book>)
}
