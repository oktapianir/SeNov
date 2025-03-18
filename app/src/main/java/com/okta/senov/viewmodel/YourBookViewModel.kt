//package com.okta.senov.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.okta.senov.model.Book
//import com.okta.senov.model.BookData
//import dagger.hilt.android.lifecycle.HiltViewModel
//import javax.inject.Inject
//
//@HiltViewModel
//class YourBookViewModel @Inject constructor() : ViewModel() {
//
//    private val _yourBooks = MutableLiveData<List<BookData>>(emptyList()) // Pakai List, bukan MutableList
//    val yourBooks: LiveData<List<BookData>> get() = _yourBooks
//
//    fun addBook(book: Book) {
//        val currentList = _yourBooks.value.orEmpty().toMutableList()
//
//        val bookData = BookData(
//            id = book.id,
//            title = book.title,
////            authorName = book.authorName,
//            image = book.coverResourceId
//        )
//
//        if (!currentList.contains(bookData)) {
//            currentList.add(bookData)
//            _yourBooks.value = currentList.toList() // Pastikan tetap List
//        }
//    }
//}
package com.okta.senov.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.okta.senov.model.Book
import com.okta.senov.model.BookData
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class YourBookViewModel @Inject constructor() : ViewModel() {

    companion object{
        // Simpan books dalam data storage
        private val _booksStorage = mutableListOf<BookData>()
    }
//
//    // Simpan books dalam data storage
//    private val _booksStorage = mutableListOf<BookData>()

    // Ekspos ke UI melalui LiveData
    private val _yourBooks = MutableLiveData<List<BookData>>()
    val yourBooks: LiveData<List<BookData>> get() = _yourBooks

    init {
        // Initialize LiveData with the current storage
        _yourBooks.value = _booksStorage.toList()
    }

    // Menambahkan buku ke daftar
    fun addBook(book: Book) {
        val bookData = BookData(
            id = book.id,
            title = book.title,
            authorName = book.authorName,
            category = book.category,
            description = book.description,
            image = book.image
        )

        // Cek apakah buku sudah ada (berdasarkan ID)
        if (_booksStorage.none { it.id == bookData.id }) {
            // Add to local storage
            _booksStorage.add(bookData)

            // Update LiveData with a new copy of the list
            // Using value instead of postValue for immediate update
            _yourBooks.value = _booksStorage.toList()

            Timber.tag("YourBookViewModel").d("Book added: ${book.title}")
            Timber.tag("YourBookViewModel").d("Current books in storage: ${_booksStorage.size}")

            // Debug the current list
            _booksStorage.forEach {
                Timber.tag("YourBookViewModel").d("Book in storage: ${it.title}, ID: ${it.id}")
            }
        } else {
            Timber.tag("YourBookViewModel").d("Book already exists: ${book.title}")
        }
    }
//    fun removeBook(bookId: Int) {
//        val currentList = _yourBooks.value.orEmpty().toMutableList()
//        val newList = currentList.filterNot { it.id == bookId }
//
//        if (currentList.size != newList.size) {
//            _yourBooks.value = newList
//            Timber.tag("YourBookViewModel").d("Book removed: $bookId")
//        } else {
//            Timber.tag("YourBookViewModel").d("Book not found: $bookId")
//        }
//    }
}