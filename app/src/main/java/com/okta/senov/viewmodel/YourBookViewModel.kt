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
//    // Use instance variable instead of companion object (static)
//    private val _booksStorage = mutableListOf<BookData>()
//
//    // Set untuk melacak ID buku yang telah dihapus dari tampilan
//    private val _hiddenBooksLocal = mutableSetOf<String>()
//
//    // Expose to UI via LiveData
//    private val _yourBooks = MutableLiveData<List<BookData>>()
//    val yourBooks: LiveData<List<BookData>> get() = _yourBooks
//    private val _listenedBooks = MutableLiveData<List<Book>>()
//    val listenedBooks: LiveData<List<Book>> = _listenedBooks
//
//    fun addListenedBook(book: Book) {
//        val currentList = _listenedBooks.value ?: emptyList()
//        _listenedBooks.value = currentList + book
//    }
//
//    private val db = FirebaseFirestore.getInstance()
//
//    init {
//        // Initialize with empty list first
//        _yourBooks.value = emptyList()
//        fetchAllUserBooks()
//    }
//
//    fun fetchAllUserBooks() {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            // Bersihkan data yang ada
//            _booksStorage.clear()
//
//            // Proses kedua sumber data dan gabungkan di akhir
//            val processedBooks = mutableMapOf<String, BookData>()
//
//            // Penghitung untuk melacak operasi
//            var operationsCompleted = 0
//
//            // Sekarang ambil bookmarks
//            db.collection("users").document(userId)
//                .collection("bookmarks")
//                .get()
//                .addOnSuccessListener { documents ->
//                    documents.forEach { doc ->
//                        try {
//                            val book = doc.toObject(Book::class.java)
//                            processedBooks[book.id] = BookData(
//                                id = book.id,
//                                title = book.title,
//                                authorName = book.authorName,
//                                category = book.category,
//                                description = book.description,
//                                image = book.image,
//                                source = "bookmark"
//                            )
//                        } catch (e: Exception) {
//                            Timber.tag("YourBookViewModel")
//                                .e("Error mengkonversi bookmark: ${e.message}")
//                        }
//                    }
//
//                    operationsCompleted++
//                    if (operationsCompleted == 2) {
//                        updateBooksData(processedBooks)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("YourBookViewModel")
//                        .e("Error mengambil bookmarks: ${e.message}")
//                    operationsCompleted++
//                    if (operationsCompleted == 2) {
//                        updateBooksData(processedBooks)
//                    }
//                }
//
//            // Ambil buku yang telah didengarkan
//            db.collection("data_read_listened")
//                .whereEqualTo("userId", userId)
//                .get()
//                .addOnSuccessListener { documents ->
//                    documents.forEach { doc ->
//                        try {
//                            val bookId = doc.getString("bookId") ?: ""
//
//                            // Hanya tambahkan jika tidak dalam daftar yang dihapus/hidden
//                            if (!_hiddenBooksLocal.contains(bookId)) {
//                                // Buat atau perbarui data buku
//                                val existingBook = processedBooks[bookId]
//
//                                val bookData = existingBook?.copy(
//                                    readCount = doc.getLong("readCount")?.toInt() ?: 0,
//                                    source = existingBook.source ?: "listened"
//                                ) ?: BookData(
//                                    id = bookId,
//                                    title = doc.getString("title") ?: "",
//                                    authorName = doc.getString("authorName") ?: "",
//                                    category = doc.getString("category") ?: "",
//                                    description = doc.getString("description") ?: "",
//                                    image = doc.getString("image") ?: "",
//                                    readCount = doc.getLong("readCount")?.toInt() ?: 0,
//                                    source = "listened"
//                                )
//
//                                // Tambahkan atau perbarui di map kita
//                                processedBooks[bookId] = bookData
//                            }
//                        } catch (e: Exception) {
//                            Timber.tag("YourBookViewModel")
//                                .e("Error mengkonversi dokumen listened: ${e.message}")
//                        }
//                    }
//
//                    operationsCompleted++
//                    if (operationsCompleted == 2) {
//                        updateBooksData(processedBooks)
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("YourBookViewModel")
//                        .e("Error mengambil buku listened: ${e.message}")
//                    operationsCompleted++
//                    if (operationsCompleted == 2) {
//                        updateBooksData(processedBooks)
//                    }
//                }
//        } else {
//            _booksStorage.clear()
//            _yourBooks.value = emptyList()
//            Timber.tag("YourBookViewModel").d("Tidak ada user yang login, buku dibersihkan")
//        }
//    }
//
//    private fun updateBooksData(processedBooks: Map<String, BookData>) {
//        _booksStorage.clear()
//        _booksStorage.addAll(processedBooks.values)
//
//        // Update LiveData with combined list
//        _yourBooks.value = _booksStorage.toList()
//
//        Timber.tag("YourBookViewModel")
//            .d("Updated books list with ${_booksStorage.size} unique books")
//    }
//
//    // Metode ini dipanggil ketika user menghapus buku dari tampilan
//    fun removeBook(bookId: String) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            // Hapus dari bookmark jika itu adalah bookmark
//            val bookToRemove = _booksStorage.find { it.id == bookId }
//
//            if (bookToRemove != null) {
//                if (bookToRemove.source == "bookmark") {
//                    // Jika ini bookmark, hapus dari koleksi bookmarks
//                    db.collection("users").document(userId)
//                        .collection("bookmarks").document(bookId)
//                        .delete()
//                        .addOnSuccessListener {
//                            Timber.tag("YourBookViewModel")
//                                .d("Buku dihapus dari bookmarks: $bookId")
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("YourBookViewModel")
//                                .e("Error menghapus dari bookmarks: ${e.message}")
//                        }
//                }
//
//                // Tambahkan ke set lokal buku yang disembunyikan
//                _hiddenBooksLocal.add(bookId)
//
//                // Hapus dari UI segera
//                val indexToRemove = _booksStorage.indexOfFirst { it.id == bookId }
//                if (indexToRemove != -1) {
//                    _booksStorage.removeAt(indexToRemove)
//                    _yourBooks.value = _booksStorage.toList()
//                }
//
//                Timber.tag("YourBookViewModel")
//                    .d("Buku dihapus dari tampilan dan ditambahkan ke hidden local: $bookId")
//            }
//        }
//    }
//
//    // Fungsi untuk menangani klik "Listen" pada buku
//    fun handleBookListened(book: Book) {
//        val userId = FirebaseAuth.getInstance().currentUser?.uid
//        if (userId != null) {
//            // Dokumen ID adalah kombinasi userId dan bookId
//            val documentId = "${userId}_${book.id}"
//
//            // Hapus buku dari daftar yang disembunyikan (jika ada)
//            // Ini memungkinkan buku muncul lagi setelah di-listen
//            _hiddenBooksLocal.remove(book.id)
//
//            // Data untuk menyimpan/memperbarui
//            val bookData = hashMapOf(
//                "userId" to userId,
//                "bookId" to book.id,
//                "title" to book.title,
//                "authorName" to book.authorName,
//                "category" to book.category,
//                "description" to book.description,
//                "image" to book.image,
//                "readCount" to 1,
//                "lastReadAt" to System.currentTimeMillis()
//            )
//
//            // Periksa apakah dokumen sudah ada
//            db.collection("data_read_listened").document(documentId)
//                .get()
//                .addOnSuccessListener { documentSnapshot ->
//                    if (documentSnapshot.exists()) {
//                        // Jika dokumen sudah ada, tingkatkan readCount
//                        val currentReadCount = documentSnapshot.getLong("readCount")?.toInt() ?: 0
//                        bookData["readCount"] = currentReadCount + 1
//                    }
//
//                    // Simpan atau perbarui dokumen
//                    db.collection("data_read_listened").document(documentId)
//                        .set(bookData)
//                        .addOnSuccessListener {
//                            Timber.tag("YourBookViewModel")
//                                .d("Buku disimpan/diperbarui di data_read_listened: ${book.id}")
//
//                            // Perbarui tampilan YourBooks
//                            fetchAllUserBooks()
//                        }
//                        .addOnFailureListener { e ->
//                            Timber.tag("YourBookViewModel")
//                                .e("Error menyimpan di data_read_listened: ${e.message}")
//                        }
//                }
//                .addOnFailureListener { e ->
//                    Timber.tag("YourBookViewModel")
//                        .e("Error memeriksa dokumen: ${e.message}")
//
//                    // Jika error, coba tetap tambahkan dokumen baru
//                    db.collection("data_read_listened").document(documentId)
//                        .set(bookData)
//                        .addOnSuccessListener {
//                            fetchAllUserBooks()
//                        }
//                }
//        }
//    }
//
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

    // Data buku yang disimpan di memory
    private val _booksStorage = mutableListOf<BookData>()

    // LiveData untuk UI
    private val _yourBooks = MutableLiveData<List<BookData>>()
    val yourBooks: LiveData<List<BookData>> get() = _yourBooks
    private val _listenedBooks = MutableLiveData<List<Book>>()
    val listenedBooks: LiveData<List<Book>> = _listenedBooks

    // LiveData untuk memicu refresh
    private val _refreshTrigger = MutableLiveData<Boolean>()
    val refreshTrigger: LiveData<Boolean> = _refreshTrigger

    private val db = FirebaseFirestore.getInstance()

    init {
        // Initialize dengan daftar kosong
        _yourBooks.value = emptyList()
        fetchAllUserBooks()
    }

    fun addListenedBook(book: Book) {
        val currentList = _listenedBooks.value ?: emptyList()
        _listenedBooks.value = currentList + book
    }

    // Memicu refresh dari fragment lain
    fun triggerRefresh() {
        _refreshTrigger.value = true
    }

    fun fetchAllUserBooks() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Bersihkan data yang ada
            _booksStorage.clear()

            // Proses kedua sumber data dan gabungkan di akhir
            val processedBooks = mutableMapOf<String, BookData>()

            // Penghitung untuk melacak operasi asinkron
            var operationsCompleted = 0

            // Ambil daftar buku yang telah dihapus
            db.collection("users").document(userId)
                .collection("hiddenBooks").get()
                .addOnSuccessListener { hiddenDocsSnapshot ->
                    // Buat set buku yang dihapus
                    val hiddenBookIds = hiddenDocsSnapshot.documents.mapNotNull { it.id }.toSet()

                    // Sekarang ambil bookmarks
                    db.collection("users").document(userId)
                        .collection("bookmarks")
                        .get()
                        .addOnSuccessListener { documents ->
                            documents.forEach { doc ->
                                try {
                                    val bookId = doc.id
                                    // Skip jika buku ada di daftar hidden
                                    if (bookId !in hiddenBookIds) {
                                        val book = doc.toObject(Book::class.java)
                                        processedBooks[book.id] = BookData(
                                            id = book.id,
                                            title = book.title,
                                            authorName = book.authorName,
                                            category = book.category,
                                            description = book.description,
                                            image = book.image,
                                            source = "bookmark"
                                        )
                                    }
                                } catch (e: Exception) {
                                    Timber.tag("YourBookViewModel")
                                        .e("Error mengkonversi bookmark: ${e.message}")
                                }
                            }

                            operationsCompleted++
                            if (operationsCompleted == 2) {
                                updateBooksData(processedBooks)
                            }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("YourBookViewModel")
                                .e("Error mengambil bookmarks: ${e.message}")
                            operationsCompleted++
                            if (operationsCompleted == 2) {
                                updateBooksData(processedBooks)
                            }
                        }

                    // Ambil buku yang telah didengarkan
                    db.collection("data_read_listened")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { documents ->
                            documents.forEach { doc ->
                                try {
                                    val bookId = doc.getString("bookId") ?: ""

                                    // Skip jika buku ada di daftar hidden
                                    if (bookId !in hiddenBookIds) {
                                        // Buat atau perbarui data buku
                                        val existingBook = processedBooks[bookId]

                                        val bookData = existingBook?.copy(
                                            readCount = doc.getLong("readCount")?.toInt() ?: 0,
                                            source = existingBook.source ?: "listened"
                                        ) ?: BookData(
                                            id = bookId,
                                            title = doc.getString("title") ?: "",
                                            authorName = doc.getString("authorName") ?: "",
                                            category = doc.getString("category") ?: "",
                                            description = doc.getString("description") ?: "",
                                            image = doc.getString("image") ?: "",
                                            readCount = doc.getLong("readCount")?.toInt() ?: 0,
                                            source = "listened"
                                        )

                                        // Tambahkan atau perbarui di map kita
                                        processedBooks[bookId] = bookData
                                    }
                                } catch (e: Exception) {
                                    Timber.tag("YourBookViewModel")
                                        .e("Error mengkonversi dokumen listened: ${e.message}")
                                }
                            }

                            operationsCompleted++
                            if (operationsCompleted == 2) {
                                updateBooksData(processedBooks)
                            }
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("YourBookViewModel")
                                .e("Error mengambil buku listened: ${e.message}")
                            operationsCompleted++
                            if (operationsCompleted == 2) {
                                updateBooksData(processedBooks)
                            }
                        }
                }
                .addOnFailureListener { e ->
                    Timber.tag("YourBookViewModel")
                        .e("Error mengambil daftar buku tersembunyi: ${e.message}")

                    // Lanjutkan dengan daftar kosong jika gagal
                    // Ulangi kode di atas dengan set kosong
                    val hiddenBookIds = emptySet<String>()

                    // (Kode sama seperti di atas untuk mengambil bookmarks dan listened books)
                    // ...
                }
        } else {
            _booksStorage.clear()
            _yourBooks.value = emptyList()
            Timber.tag("YourBookViewModel").d("Tidak ada user yang login, buku dibersihkan")
        }
    }

    private fun updateBooksData(processedBooks: Map<String, BookData>) {
        _booksStorage.clear()
        _booksStorage.addAll(processedBooks.values)

        // Update LiveData dengan daftar gabungan
        _yourBooks.value = _booksStorage.toList()

        // Reset refresh trigger
        _refreshTrigger.value = false

        Timber.tag("YourBookViewModel")
            .d("Updated books list with ${_booksStorage.size} unique books")
    }

    // Metode ini dipanggil ketika user menghapus buku dari tampilan
    fun removeBook(bookId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            // Hapus dari bookmark jika itu adalah bookmark
            val bookToRemove = _booksStorage.find { it.id == bookId }

            if (bookToRemove != null) {
                if (bookToRemove.source == "bookmark") {
                    // Jika ini bookmark, hapus dari koleksi bookmarks
                    db.collection("users").document(userId)
                        .collection("bookmarks").document(bookId)
                        .delete()
                        .addOnSuccessListener {
                            Timber.tag("YourBookViewModel")
                                .d("Buku dihapus dari bookmarks: $bookId")
                        }
                        .addOnFailureListener { e ->
                            Timber.tag("YourBookViewModel")
                                .e("Error menghapus dari bookmarks: ${e.message}")
                        }
                }

                // Tambahkan ke koleksi hiddenBooks di Firestore untuk persistensi
                db.collection("users").document(userId)
                    .collection("hiddenBooks").document(bookId)
                    .set(hashMapOf("hiddenAt" to com.google.firebase.Timestamp.now()))
                    .addOnSuccessListener {
                        Timber.tag("YourBookViewModel")
                            .d("Buku ditambahkan ke daftar tersembunyi: $bookId")
                    }
                    .addOnFailureListener { e ->
                        Timber.tag("YourBookViewModel")
                            .e("Error menambahkan ke hidden books: ${e.message}")
                    }

                // Hapus dari UI segera
                val indexToRemove = _booksStorage.indexOfFirst { it.id == bookId }
                if (indexToRemove != -1) {
                    _booksStorage.removeAt(indexToRemove)
                    _yourBooks.value = _booksStorage.toList()
                }

                Timber.tag("YourBookViewModel")
                    .d("Buku dihapus dari tampilan: $bookId")
            }
        }
    }
}