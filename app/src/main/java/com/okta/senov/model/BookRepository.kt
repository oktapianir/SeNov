package com.okta.senov.model

import com.okta.senov.data.BookDao
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@ViewModelScoped
class BookRepository @Inject constructor(private val bookDao: BookDao) {

    suspend fun getBooks(): List<Book> {
        return bookDao.getAllBooks()
    }

    suspend fun insertBooks(books: List<Book>) {
        bookDao.insertBooks(books)
    }
}
