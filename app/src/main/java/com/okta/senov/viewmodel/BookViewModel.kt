package com.okta.senov.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.model.Author
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.model.BookData
import com.okta.senov.repository.BookContentRepository
import com.okta.senov.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookContentRepository: BookContentRepository
) : ViewModel() {

    //    private val _popularBooks = MutableLiveData<List<BookData>>()
//    val popularBooks: LiveData<List<BookData>> = _popularBooks
//
//    private val _allBooks = MutableLiveData<List<Book>>()
//    val allBooks: LiveData<List<Book>> = _allBooks
    private val _popularBooks = MutableLiveData<List<BookData>>(emptyList())
    val popularBooks: LiveData<List<BookData>> get() = _popularBooks

    private val _allBooks = MutableLiveData<List<BookData>>(emptyList())
    val allBooks: LiveData<List<BookData>> get() = _allBooks


    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> get() = _authors

    private val _bookContent = MutableLiveData<List<BookContent>>(emptyList())
    val bookContent: LiveData<List<BookContent>> get() = _bookContent

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()


//    fun fetchBooksFromApi(apiKey: String, query: String = "") {
//        _loading.postValue(true)
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val bookDataList = bookRepository.fetchBooks(apiKey, query)
//
//                _popularBooks.postValue(bookDataList.take(10))
//
//                _allBooks.postValue(bookDataList.map { bookData ->
//                    Book(
//                        id = bookData.id,
//                        title = bookData.title,
//                        coverResourceId = bookData.image
//                    )
//                })
//
//                Timber.tag("API_SUCCESS")
//                    .d("Books fetched successfully: ${bookDataList.size} books received.")
//            } catch (e: Exception) {
//                _errorMessage.postValue("Error fetching books: ${e.message}")
//                Timber.tag("BookViewModel").e("Error fetching books")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }

//    fun fetchBooksFromApi(apiKey: String, query: String = "") {
//        _loading.postValue(true)
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Ambil data buku dan penulis dari API
//                val bookDataList = bookRepository.fetchBooks(apiKey, query)
//                val authorDataList = bookRepository.fetchAuthors(apiKey, query)
//
//                _popularBooks.postValue(bookDataList.take(10))
//
//                _allBooks.postValue(bookDataList.map { bookData ->
//                    // Cari author berdasarkan nama
////                    val author = authorDataList.find { it.name == bookData.authorName }
//
//                    Book(
//                        id = bookData.id,
//                        title = bookData.title,
////                        authorName = author?.name ?: "Unknown Author", // Pastikan ada author
//                        coverResourceId = bookData.image
//                    )
//                })
//
//                Timber.tag("API_SUCCESS")
//                    .d("Books fetched successfully: ${bookDataList.size} books received.")
//            } catch (e: Exception) {
//                _errorMessage.postValue("Error fetching books: ${e.message}")
//                Timber.tag("BookViewModel").e("Error fetching books")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }

    fun fetchBooksFromApi(apiKey: String, query: String = "") {
        _loading.postValue(true)
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                // Ambil data buku dan penulis dari API
//                val bookDataList = bookRepository.fetchBooks(apiKey, query).orEmpty()
//                val authorDataList = bookRepository.fetchAuthors(apiKey, query).orEmpty()
//
//                // Update LiveData popularBooks dengan 10 buku pertama
//                _popularBooks.postValue(bookDataList.take(10))
//
//                // Konversi bookDataList menjadi daftar Book yang valid
//                val books = bookDataList.map { bookData ->
//                    Book(
//                        id = bookData.id,
//                        title = bookData.title,
//                        coverResourceId = bookData.image ?: "" // Hindari null
//                    )
//                }
//
//                // Update LiveData allBooks dengan daftar yang sudah dikonversi
//                _allBooks.postValue(bookDataList)
//
//                Timber.tag("API_SUCCESS")
//                    .d("Books fetched successfully: ${bookDataList.size} books received.")
//            } catch (e: Exception) {
//                _errorMessage.postValue("Error fetching books: ${e.message}")
//                Timber.tag("BookViewModel").e("Error fetching books: ${e.message}")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
        // In BookViewModel, modify fetchBooksFromApi method:
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get books from Firestore
                val bookDataList = bookRepository.getAllBooks()

                // Log each book to verify data
                bookDataList.forEach { book ->
                    Timber.tag("BOOK_DATA")
                        .d("Book: ${book.title}, Author: ${book.authorName}, Image: ${book.image}")
                }

                // Update LiveData
                _popularBooks.postValue(bookDataList.take(10))
                _allBooks.postValue(bookDataList)

                Timber.tag("FIRESTORE_SUCCESS")
                    .d("Books fetched successfully: ${bookDataList.size} books received.")
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching books: ${e.message}")
                Timber.tag("BookViewModel").e("Error fetching books: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }
    fun fetchBooksFromFirestore() {
        viewModelScope.launch {
            try {
                Timber.d("Memulai fetch buku dari Firestore")
                firestore.collection("Books")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        Timber.d("Mendapatkan snapshot dari Firestore: ${snapshot.documents.size} dokumen")

                        // Log untuk melihat raw data
                        snapshot.documents.forEachIndexed { index, doc ->
                            Timber.d("Dokumen[$index] ID: ${doc.id}, Data: ${doc.data}")
                        }

                        val booksList = snapshot.documents.mapNotNull { doc ->
                            // Konversi Book ke BookData
                            val book = doc.toObject(Book::class.java)
                            Timber.d("Konversi dokumen ${doc.id} ke Book: ${book != null}")

                            book?.let {
                                val bookData = BookData(
                                    id = doc.id,
                                    title = it.title,
                                    authorName = it.authorName,
                                    category = it.category,
                                    image = it.image
                                )
                                Timber.d("Konversi Book ke BookData berhasil: $bookData")
                                bookData
                            }
                        }

                        Timber.d("Jumlah buku setelah konversi: ${booksList.size}")
                        _allBooks.value = booksList
                        Timber.d("Nilai _allBooks.value telah diperbarui dengan ${booksList.size} buku")

                        // Memeriksa nilai _allBooks setelah diperbarui
                        Timber.d("Nilai _allBooks sekarang: ${_allBooks.value?.size} buku")
                    }
                    .addOnFailureListener { e ->
                        Timber.e("Error mengambil buku: ${e.message}")
                        e.printStackTrace()
                    }
            } catch (e: Exception) {
                Timber.e("Exception saat mengambil buku: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    //    fun fetchAuthors(apiKey: String, authorName: String) {
//        _loading.postValue(true)
//        viewModelScope.launch(Dispatchers.IO) {
//            try {
//                val authorsList = bookRepository.fetchAuthors(apiKey, authorName)
//                _authors.postValue(authorsList)
//            } catch (e: Exception) {
//                _errorMessage.postValue("Error fetching authors: ${e.message}")
//                Timber.tag("BookViewModel").e("Error fetching authors")
//            } finally {
//                _loading.postValue(false)
//            }
//        }
//    }
//    fun fetchAuthorsFromApi(apiKey: String, authorName: String) {
//        viewModelScope.launch {
//            try {
//                val authorsList = bookRepository.getAllAuthors(apiKey, authorName)
//                _authors.value = authorsList
//            } catch (e: Exception) {
//                Timber.e(e, "Error fetching authors")
//            }
//        }
//    }

    //    fun loadBookContent(bookId: String) {
//        _loading.value = true
//        viewModelScope.launch {
//            try {
//                val content = bookContentRepository.getBookContent(bookId)
//                if (content != null) {
//                    _bookContent.value = listOf(content)
//                } else {
//                    _errorMessage.value = "Tidak dapat menemukan isi buku"
//                }
//            } catch (e: Exception) {
//                _errorMessage.value = "Error: ${e.message}"
//            } finally {
//                _loading.value = false
//            }
//        }
//    }
    fun loadBookContent(bookId: String) {
        _loading.value = true
        Timber.d("Fetching book content for ID: $bookId")

        bookContentRepository.getBookContent(bookId) { content, error ->
            if (content != null) {
                _bookContent.value = listOf(content)
                Timber.d("Book content fetched successfully: $content")
            } else {
                _errorMessage.value = error ?: "Tidak dapat menemukan isi buku"
                Timber.e("Error fetching book content: ${error ?: "Dokumen tidak ditemukan"}")
            }
            _loading.value = false
        }
    }


}
