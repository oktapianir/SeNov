//package com.okta.senov.viewmodel
//
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.okta.senov.model.Book
//import com.okta.senov.model.BookData
//import dagger.hilt.android.lifecycle.HiltViewModel
//import timber.log.Timber
//import javax.inject.Inject
//
//@HiltViewModel
//class YourBookViewModel @Inject constructor() : ViewModel() {
//
//    companion object{
//        // Simpan books dalam data storage
//        private val _booksStorage = mutableListOf<BookData>()
//    }
////
////    // Simpan books dalam data storage
////    private val _booksStorage = mutableListOf<BookData>()
//
//    // Ekspos ke UI melalui LiveData
//    private val _yourBooks = MutableLiveData<List<BookData>>()
//    val yourBooks: LiveData<List<BookData>> get() = _yourBooks
//
//    private val db = FirebaseFirestore.getInstance()
//
//    init {
//        // Initialize LiveData with the current storage
//        _yourBooks.value = _booksStorage.toList()
//        fetchBookmarkedBooks()
//    }
//
//    fun fetchBookmarkedBooks() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks")
//                .get()
//                .addOnSuccessListener { documents ->
//                    val booksList = documents.mapNotNull { doc ->
//                        doc.toObject(Book::class.java)
//                    }
//                    // Konversi dari Book ke BookData
//                    _yourBooks.value = booksList.map { book ->
//                        BookData(
//                            id = book.id,
//                            title = book.title,
//                            authorName = book.authorName,
//                            category = book.category,
//                            description = book.description,
//                            image = book.image
//                        )
//                    }
//                    Timber.tag("YourBookViewModel").d("Fetched ${booksList.size} bookmarked books")
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("YourBookViewModel").e("Error fetching bookmarks: ${e.message}")
//                    _yourBooks.value = emptyList()
//                }
//        } else {
//            // Handle user not logged in
//            _yourBooks.value = emptyList()
//        }
//    }
//
//    fun removeBook(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            db.collection("users").document(userId)
//                .collection("bookmarks").document(bookId)
//                .delete()
//                .addOnSuccessListener {
//                    // Refresh the list after deletion
//                    fetchBookmarkedBooks()
//                    Timber.tag("YourBookViewModel").d("Book removed from bookmarks: $bookId")
//                }
//        }
//    }
//
//    // Menambahkan buku ke daftar
//    fun addBook(book: Book) {
//        val bookData = BookData(
//            id = book.id,
//            title = book.title,
//            authorName = book.authorName,
//            category = book.category,
//            description = book.description,
//            image = book.image
//        )
//
//        // Cek apakah buku sudah ada (berdasarkan ID)
//        if (_booksStorage.none { it.id == bookData.id }) {
//            // Add to local storage
//            _booksStorage.add(bookData)
//
//            // Update LiveData with a new copy of the list
//            // Using value instead of postValue for immediate update
//            _yourBooks.value = _booksStorage.toList()
//
//            Timber.tag("YourBookViewModel").d("Book added: ${book.title}")
//            Timber.tag("YourBookViewModel").d("Current books in storage: ${_booksStorage.size}")
//
//            // Debug the current list
//            _booksStorage.forEach {
//                Timber.tag("YourBookViewModel").d("Book in storage: ${it.title}, ID: ${it.id}")
//            }
//        } else {
//            Timber.tag("YourBookViewModel").d("Book already exists: ${book.title}")
//        }
//    }
////    fun removeBook(bookId: Int) {
////        val currentList = _yourBooks.value.orEmpty().toMutableList()
////        val newList = currentList.filterNot { it.id == bookId }
////
////        if (currentList.size != newList.size) {
////            _yourBooks.value = newList
////            Timber.tag("YourBookViewModel").d("Book removed: $bookId")
////        } else {
////            Timber.tag("YourBookViewModel").d("Book not found: $bookId")
////        }
////    }
//}

package com.okta.senov.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.model.Book
import com.okta.senov.model.BookData
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class YourBookViewModel @Inject constructor() : ViewModel() {

    // Use instance variable instead of companion object (static)
    private val _booksStorage = mutableListOf<BookData>()

    // Expose to UI via LiveData
    private val _yourBooks = MutableLiveData<List<BookData>>()
    val yourBooks: LiveData<List<BookData>> get() = _yourBooks

    private val db = FirebaseFirestore.getInstance()

    init {
        // Initialize with empty list first
        _yourBooks.value = emptyList()
        fetchBookmarkedBooks()
    }

    fun fetchBookmarkedBooks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("bookmarks")
                .get()
                .addOnSuccessListener { documents ->
                    val booksList = documents.mapNotNull { doc ->
                        doc.toObject(Book::class.java)
                    }

                    // Clear any existing data in storage
                    _booksStorage.clear()

                    // Convert from Book to BookData and store
                    val bookDataList = booksList.map { book ->
                        BookData(
                            id = book.id,
                            title = book.title,
                            authorName = book.authorName,
                            category = book.category,
                            description = book.description,
                            image = book.image
                        )
                    }

                    // Add to storage
                    _booksStorage.addAll(bookDataList)

                    // Update LiveData
                    _yourBooks.value = _booksStorage.toList()

                    Timber.tag("YourBookViewModel")
                        .d("Fetched ${booksList.size} bookmarked books for user $userId")
                }
                .addOnFailureListener { e ->
                    Timber.tag("YourBookViewModel").e("Error fetching bookmarks: ${e.message}")
                    _booksStorage.clear()
                    _yourBooks.value = emptyList()
                }
        } else {
            // Handle user not logged in
            _booksStorage.clear()
            _yourBooks.value = emptyList()
            Timber.tag("YourBookViewModel").d("No user logged in, cleared bookmarks")
        }
    }

    fun removeBook(bookId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("bookmarks").document(bookId)
                .delete()
                .addOnSuccessListener {
                    // Refresh the list after deletion
                    fetchBookmarkedBooks()
                    Timber.tag("YourBookViewModel").d("Book removed from bookmarks: $bookId")
                }
        }
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

    // Clear data when user logs out
    fun clearData() {
        _booksStorage.clear()
        _yourBooks.value = emptyList()
        Timber.tag("YourBookViewModel").d("Book data cleared")
    }
}