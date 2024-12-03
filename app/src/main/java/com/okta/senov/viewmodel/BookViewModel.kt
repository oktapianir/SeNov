package com.okta.senov.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.okta.senov.model.Book
import com.okta.senov.model.BookRepository
import com.okta.senov.data.BookDatabase
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val bookRepository: BookRepository
    private val _popularBooks = MutableLiveData<List<Book>>()
    val popularBooks: LiveData<List<Book>> = _popularBooks

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> = _allBooks

    init {
        val bookDao = BookDatabase.getDatabase(application).bookDao()
        bookRepository = BookRepository(bookDao)

        fetchBooks()
    }

    private fun fetchBooks() {
        viewModelScope.launch {
            // Mengambil semua buku dari repositori
            val allBooksList = bookRepository.getBooks()

            // Pisahkan buku berdasarkan rating
            val popular = allBooksList.filter { it.rating >= 4.5f }
            val all = allBooksList.filter { it.rating < 4.5f }

            // Update LiveData setelah data diproses
            _popularBooks.postValue(popular.ifEmpty { listOf() })
            _allBooks.postValue(all.ifEmpty { allBooksList }) // Menampilkan semua jika tidak ada popular
        }
    }
}
