package com.okta.senov.viewmodel

//import android.media.Rating
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.okta.senov.model.Author
import com.okta.senov.model.Book
import com.okta.senov.model.BookContent
import com.okta.senov.model.BookData
import com.okta.senov.repository.BookContentRepository
import com.okta.senov.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.okta.senov.model.Rating
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val bookContentRepository: BookContentRepository
) : ViewModel() {

    private val _popularBooks = MutableLiveData<List<BookData>>(emptyList())
    val popularBooks: LiveData<List<BookData>> get() = _popularBooks

    private val _allBooks = MutableLiveData<List<BookData>>(emptyList())
    val allBooks: LiveData<List<BookData>> get() = _allBooks


    private val _authors = MutableLiveData<List<Author>>()
    val authors: LiveData<List<Author>> get() = _authors

    private val _bookContent = MutableLiveData<List<BookContent>>(emptyList())
    val bookContent: LiveData<List<BookContent>> get() = _bookContent

    private val _selectedAuthor = MutableLiveData<Author>()
    val selectedAuthor: LiveData<Author> = _selectedAuthor

    private val _searchResults = MutableLiveData<List<Book>>()
    val searchResults: LiveData<List<Book>> = _searchResults

    // Add a new LiveData for book ratings
    private val _bookRatings = MutableLiveData<Map<String, Float>>()
    val bookRatings: LiveData<Map<String, Float>> = _bookRatings

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val firestore = FirebaseFirestore.getInstance()

    fun fetchBooksFromApi(apiKey: String, query: String = "") {
        _loading.postValue(true)
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

    fun fetchAllBookContents() {
        _loading.value = true
        firestore.collection("bookContents")
            .get()
            .addOnSuccessListener { snapshot ->
                val contentList = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(BookContent::class.java)
                }
                _bookContent.value = contentList
                _loading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Error fetching book content: ${e.message}"
                _loading.value = false
            }
    }

    fun fetchTopAuthorsFromFirebase() {
        Timber.tag("BookViewModel").d("Mulai mengambil data authors dari Firebase")
        firestore.collection("authors")
            .orderBy("nameAuthor", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { documents ->
                Timber.tag("BookViewModel").d("Berhasil mengambil ${documents.size()} authors")
                val authorsList = mutableListOf<Author>()
                for (document in documents) {
                    val author = document.toObject(Author::class.java).copy(id = document.id)
                    authorsList.add(author)
                    Timber.tag("BookViewModel").d("Author: ${author.nameAuthor}")
                }
                _authors.postValue(authorsList)
            }
            .addOnFailureListener { e ->
                Timber.tag("BookViewModel").e(e, "Error mengambil authors")
            }
    }

    // Function to fetch a single author by ID
    fun fetchAuthorById(authorId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("authors").document(authorId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val author = document.toObject(Author::class.java)
                    _selectedAuthor.value = author!!
                } else {
                    Timber.e("Dokumen author tidak ditemukan atau gagal dikonversi.")
                }
            }
            .addOnFailureListener { exception ->
                Timber.e("Error getting author: $exception")
            }
    }

    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    fun searchBooks(query: String) {
        Timber.d("Search initiated with query: '$query'")

        // Enhanced logging and matching
        firestore.collection("Books")
            .get()
            .addOnSuccessListener { snapshot ->
                Timber.d("Total books for search: ${snapshot.size()}")

                // More detailed logging of book details
                snapshot.documents.forEach { document ->
                    val book = document.toObject(Book::class.java)
                    Timber.d("Book Details - Title: ${book?.title}, Author: ${book?.authorName}")
                }

                // More flexible search matching
                val lowercaseQuery = query.lowercase()
                val searchResults = snapshot.documents.filter { document ->
                    val book = document.toObject(Book::class.java)
                    book?.let {
                        it.title.lowercase().contains(lowercaseQuery) ||
                                it.authorName.lowercase().contains(lowercaseQuery) ||
                                it.category.lowercase().contains(lowercaseQuery)
                    } ?: false
                }

                // Rest of the search implementation...
            }
    }

    // Diagnostic method to check Firestore connection and collection
    fun diagnosticFirestoreCheck() {
        firestore.collection("Books")
            .get()
            .addOnSuccessListener { snapshot ->
                Timber.d("Diagnostic Check - Total Books: ${snapshot.size()}")
                snapshot.documents.forEach { document ->
                    Timber.d("Diagnostic - Document: ${document.id}, Data: ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Diagnostic Check Failed")
            }
    }

    // Ensure the fetchRatingsFromFirestore method logs more data
    fun fetchRatingsFromFirestore() {
        viewModelScope.launch {
            try {
                Timber.d("Starting to fetch ratings from Firestore")
                val ratingsRef = FirebaseFirestore.getInstance().collection("ratings")

                ratingsRef.get().addOnSuccessListener { result ->
                    Timber.d("Retrieved ${result.size()} rating documents")

                    // Debug: Print all raw ratings data
                    for (document in result) {
                        val data = document.data
                        Timber.d("Raw rating data: $data")
                    }

                    val ratingsMap = mutableMapOf<String, MutableList<Float>>()

                    for (document in result) {
                        val rating = document.toObject(Rating::class.java)

                        // Use correct field name 'rating' instead of 'ratingValue'
                        Timber.d("Rating document: bookId=${rating.bookId}, value=${rating.rating}")

                        if (!ratingsMap.containsKey(rating.bookId)) {
                            ratingsMap[rating.bookId] = mutableListOf()
                        }

                        // Add the rating value (using 'rating' field)
                        ratingsMap[rating.bookId]?.add(rating.rating)
                    }

                    Timber.d("Compiled ratings for ${ratingsMap.size} unique books")

                    // Calculate average rating for each book
                    val avgRatings = mutableMapOf<String, Float>()
                    ratingsMap.forEach { (bookId, ratings) ->
                        avgRatings[bookId] = ratings.average().toFloat()
                        Timber.d("Book $bookId average rating: ${avgRatings[bookId]}")
                    }

                    _bookRatings.value = avgRatings
                    Timber.d("Published ${avgRatings.size} average ratings to LiveData")
                }.addOnFailureListener { exception ->
                    Timber.e(exception, "Error getting ratings")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error fetching ratings")
            }
        }
    }
}
