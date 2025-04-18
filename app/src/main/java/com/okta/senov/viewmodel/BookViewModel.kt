package com.okta.senov.viewmodel

//import android.media.Rating
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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
    private val bookContentRepository: BookContentRepository,

    ) : ViewModel() {
    // Ganti USER_ID statis dengan fungsi yang mendapatkan ID pengguna saat ini
    private fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user"
    }

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

    fun fetchPopularBooksFromFirestore() {
        Timber.d("Fetching most read books from Firestore")

        // Step 1: Query data_read_listened collection to count books by read frequency
        firestore.collection("data_read_listened")
            .get()
            .addOnSuccessListener { readSnapshot ->
                // Count the frequency of each book being read
                val bookReadCounts = mutableMapOf<String, Int>()

                for (document in readSnapshot.documents) {
                    val bookId = document.getString("bookId") ?: continue
                    bookReadCounts[bookId] = (bookReadCounts[bookId] ?: 0) + 1
                }

                Timber.d("Counted read frequencies for ${bookReadCounts.size} books")

                // Get the top 6 most read book IDs (limit to exactly 6)
                val topBookIds = bookReadCounts.entries
                    .sortedByDescending { it.value }
                    .take(6)  // Ensure we only get 6 books
                    .map { it.key }

                if (topBookIds.isEmpty()) {
                    Timber.d("No read data found, falling back to default book listing")
                    fetchDefaultPopularBooks()
                    return@addOnSuccessListener
                }

                // Step 2: Get book details for the top 6 most read books
                firestore.collection("Books")
                    .get()
                    .addOnSuccessListener { booksSnapshot ->
                        val topBooks = mutableListOf<BookData>()

                        // For each top book ID, find its document and convert to BookData
                        for (bookId in topBookIds) {
                            val bookDoc = booksSnapshot.documents.find { it.id == bookId }

                            bookDoc?.let { document ->
                                try {
                                    val title = document.getString("titleBook") ?: ""
                                    val author = document.getString("authorName") ?: ""
                                    val coverUrl = document.getString("fotoUrl") ?: ""
                                    val description = document.getString("bookDescription") ?: ""
                                    val category = document.getString("nameCategory") ?: ""
                                    val readCount = bookReadCounts[bookId] ?: 0

                                    // Convert to BookData
                                    val bookData = BookData(
                                        id = document.id,
                                        title = title,
                                        authorName = author,
                                        category = category,
                                        description = description,
                                        image = coverUrl
                                    )

                                    topBooks.add(bookData)
                                    Timber.d("Added book '${bookData.title}' with read count: $readCount")
                                } catch (e: Exception) {
                                    Timber.e(e, "Error parsing book document")
                                }
                            }
                        }

                        // Ensure books are still ordered by read count
                        val sortedTopBooks = topBooks.sortedBy { book ->
                            // Sort based on the position in topBookIds (which is already sorted by read count)
                            topBookIds.indexOf(book.id)
                        }

                        Timber.d("Fetched ${sortedTopBooks.size} most read books from Firestore")
                        _popularBooks.postValue(sortedTopBooks)
                    }
                    .addOnFailureListener { e ->
                        Timber.e(e, "Error fetching book details for most read books")
                        fetchDefaultPopularBooks()
                    }
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error fetching read data from Firestore")
                fetchDefaultPopularBooks()
            }
    }


    // Fallback method to fetch books if read data can't be retrieved
    private fun fetchDefaultPopularBooks() {
        firestore.collection("Books")
            .limit(6)
            .get()
            .addOnSuccessListener { snapshot ->
                val booksList = snapshot.documents.mapNotNull { document ->
                    try {
                        val id = document.id
                        val title = document.getString("titleBook") ?: ""
                        val author = document.getString("nameAuthor") ?: ""
                        val coverUrl = document.getString("fotoUrl") ?: ""
                        val description = document.getString("bookDescription") ?: ""
                        val category = document.getString("nameCategory") ?: ""

                        // Convert to BookData
                        BookData(
                            id = id,
                            title = title,
                            authorName = author,
                            category = category,
                            description = description,
                            image = coverUrl
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing book document")
                        null
                    }
                }

                Timber.d("Fetched ${booksList.size} default books from Firestore")
                _popularBooks.postValue(booksList)
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Error fetching default books from Firestore")
                _popularBooks.postValue(emptyList())
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
                    val author = document.toObject(Author::class.java).copy(idAuthor = document.id)
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

    // Fungsi untuk menambahkan penulis ke favorit
    fun addAuthorToFavorites(author: Author) {
        // Membuat data favorit author
        val favoriteAuthorData = hashMapOf(
            "authorId" to author.idAuthor,
            "nameAuthor" to author.nameAuthor,
            "userId" to getCurrentUserId(),
            "timestamp" to System.currentTimeMillis()
        )

        // Menyimpan di koleksi root "favoriteAuthors"
        firestore.collection("favoriteAuthors")
            .add(favoriteAuthorData)
            .addOnSuccessListener {
                Timber.d("Author successfully added to favorites with ID: ${it.id}")
            }
            .addOnFailureListener { e ->
                Timber.e("Error adding author to favorites: ${e.message}")
            }
    }

    // Fungsi untuk menghapus penulis dari favorit
    fun removeAuthorFromFavorites(authorId: String) {
        // Mencari dokumen favorit yang sesuai dengan authorId dan userId
        firestore.collection("favoriteAuthors")
            .whereEqualTo("authorId", authorId)
            .whereEqualTo("userId", getCurrentUserId())
            .get()
            .addOnSuccessListener { documents ->
                // Hapus semua dokumen yang cocok
                for (document in documents) {
                    document.reference.delete()
                    Timber.d("Author successfully removed from favorites: ${document.id}")
                }
            }
            .addOnFailureListener { e ->
                Timber.e("Error removing author from favorites: ${e.message}")
            }
    }

    // Fungsi untuk memeriksa apakah penulis sudah difavoritkan
    fun isAuthorFavorite(authorId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        firestore.collection("favoriteAuthors")
            .whereEqualTo("authorId", authorId)
            .whereEqualTo("userId", getCurrentUserId())
            .get()
            .addOnSuccessListener { documents ->
                result.value = !documents.isEmpty
            }
            .addOnFailureListener { e ->
                Timber.e("Error checking favorite status: ${e.message}")
                result.value = false
            }

        return result
    }

    // Fungsi untuk mendapatkan semua penulis favorit
    fun getFavoriteAuthors(): LiveData<List<Author>> {
        val favoriteAuthors = MutableLiveData<List<Author>>()

        firestore.collection("favoriteAuthors")
            .whereEqualTo("userId", getCurrentUserId())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e("Error getting favorite authors: ${error.message}")
                    favoriteAuthors.value = emptyList()
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Dapatkan ID penulis dari dokumen favorit
                    val authorIds = snapshot.documents.mapNotNull { it.getString("authorId") }

                    if (authorIds.isNotEmpty()) {
                        // Ambil data penulis lengkap dari koleksi authors berdasarkan ID
                        val authorsList = mutableListOf<Author>()
                        var loadedCount = 0

                        for (authorId in authorIds) {
                            firestore.collection("authors")
                                .document(authorId)
                                .get()
                                .addOnSuccessListener { authorDoc ->
                                    val author = authorDoc.toObject(Author::class.java)?.copy(idAuthor = authorDoc.id)
                                    if (author != null) {
                                        authorsList.add(author)
                                    }

                                    loadedCount++
                                    // Jika semua penulis sudah dimuat, perbarui LiveData
                                    if (loadedCount == authorIds.size) {
                                        favoriteAuthors.value = authorsList
                                    }
                                }
                        }
                    } else {
                        favoriteAuthors.value = emptyList()
                    }
                }
            }

        return favoriteAuthors
    }

    fun selectAuthor(author: Author) {
        _selectedAuthor.value = author
    }

    fun searchBooks(query: String) {
        _loading.value = true
        Timber.d("Search initiated with query: '$query'")

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _loading.value = false
            return
        }

        firestore.collection("Books")
            .get()
            .addOnSuccessListener { snapshot ->
                Timber.d("Total books for search: ${snapshot.size()}")

                val lowercaseQuery = query.lowercase().trim()
                val results = snapshot.documents.mapNotNull { document ->
                    try {
                        val id = document.id
                        val title = document.getString("titleBook") ?: ""
                        val author =
                            document.getString("authorName") ?: document.getString("nameAuthor")
                            ?: ""
                        val category =
                            document.getString("nameCategory") ?: document.getString("category")
                            ?: ""
                        val coverUrl =
                            document.getString("fotoUrl") ?: document.getString("image") ?: ""
                        val description = document.getString("bookDescription") ?: ""

                        // Check if the book matches the search criteria
                        val matchesTitle = title.lowercase().contains(lowercaseQuery)
                        val matchesAuthor = author.lowercase().contains(lowercaseQuery)
                        val matchesCategory = category.lowercase().contains(lowercaseQuery)

                        if (matchesTitle || matchesAuthor || matchesCategory) {
                            Timber.d("Match found - Title: $title, Author: $author, Category: $category")
                            Book(
                                id = id,
                                title = title,
                                authorName = author,
                                category = category,
                                description = description,
                                image = coverUrl
                            )
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error parsing book document during search")
                        null
                    }
                }

                Timber.d("Search completed. Found ${results.size} matching books")
                _searchResults.value = results
                _loading.value = false
            }
            .addOnFailureListener { exception ->
                Timber.e(exception, "Search failed")
                _errorMessage.value = "Error searching books: ${exception.message}"
                _searchResults.value = emptyList()
                _loading.value = false
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
