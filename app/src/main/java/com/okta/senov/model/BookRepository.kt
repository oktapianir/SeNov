package com.okta.senov.model

import com.okta.senov.data.BookDao

class BookRepository(private val bookDao: BookDao) {

    suspend fun getBooks(): List<Book> {
        return bookDao.getAllBooks()
    }
}
