package com.okta.senov.repository

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.model.BookData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val context: Context,
    private val firestore: FirebaseFirestore
) {
    // Metode untuk mengambil semua buku dari Firestore
//    suspend fun getAllBooks(): List<BookData> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val snapshot = firestore.collection("books").get().await()
//                val books = snapshot.documents.mapNotNull { document ->
//                    try {
//                        BookData(
//                            id = document.id,
//                            title = document.getString("title") ?: "",
//                            image = document.getString("image") ?: ""
//                            // Tambahkan field lain yang ada di Firestore
//                        )
//                    } catch (e: Exception) {
//                        Timber.tag("FIRESTORE_ERROR").e(e, "Error mengkonversi dokumen")
//                        null
//                    }
//                }
//                Timber.tag("FIRESTORE_SUCCESS").d("Books received from Firestore: ${books.size}")
//                books
//            } catch (e: Exception) {
//                Timber.tag("FIRESTORE_ERROR").e(e, "Error mengambil buku dari Firestore")
//                emptyList()
//            }
//        }
//    }

    // Inside BookRepository class
//    suspend fun getAllBooks(): List<BookData> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val snapshot = firestore.collection("books").get().await()
//                val books = snapshot.documents.mapNotNull { document ->
//                    try {
//                        BookData(
//                            id = document.id,
//                            title = document.getString("title") ?: "",
//                            image = document.getString("image") ?: "",
//                            authorName = document.getString("authorName") ?: "",
//                            category = document.getString("genre") ?: "",
//                            description = document.getString("synopsis") ?: ""
//                        )
//                    } catch (e: Exception) {
//                        Timber.tag("FIRESTORE_ERROR").e(e, "Error mengkonversi dokumen")
//                        null
//                    }
//                }
//                Timber.tag("FIRESTORE_SUCCESS").d("Books received from Firestore: ${books.size}")
//                books
//            } catch (e: Exception) {
//                Timber.tag("FIRESTORE_ERROR").e(e, "Error mengambil buku dari Firestore")
//                emptyList()
//            }
//        }
//    }

    suspend fun getAllBooks(): List<BookData> {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("Books").get().await()
                val books = snapshot.documents.mapNotNull { document ->
                    try {
                        BookData(
                            id = document.getString("idBook") ?: document.id,
                            title = document.getString("titleBook") ?: "",
                            image = document.getString("fotoUrl") ?: "",
                            authorName = document.getString("nameAuthor") ?: "",
                            category = document.getString("nameCategory") ?: "",
                            description = document.getString("bookDescription") ?: ""
                        )
                    } catch (e: Exception) {
                        Timber.tag("FIRESTORE_ERROR").e(e, "Error converting document")
                        null
                    }
                }
                Timber.tag("FIRESTORE_SUCCESS").d("Books received from Firestore: ${books.size}")
                books
            } catch (e: Exception) {
                Timber.tag("FIRESTORE_ERROR").e(e, "Error fetching books from Firestore")
                emptyList()
            }
        }
    }

    // Metode dengan callback
    fun getAllBooks(callback: ApiCallback<List<BookData>>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val books = getAllBooks()
                withContext(Dispatchers.Main) {
                    callback.onSuccess(books)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Error mengambil buku: ${e.message}")
                }
            }
        }
    }

    // Metode untuk mencari buku berdasarkan judul
    fun searchBooks(query: String, callback: ApiCallback<List<BookData>>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Lakukan pencarian case-insensitive
                val snapshot = firestore.collection("books")
                    .whereGreaterThanOrEqualTo("title", query)
                    .whereLessThanOrEqualTo("title", query + "\uf8ff")
                    .get()
                    .await()

                val books = snapshot.documents.mapNotNull { document ->
                    try {
                        BookData(
                            id = document.id,
                            title = document.getString("title") ?: "",
                            image = document.getString("image") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                withContext(Dispatchers.Main) {
                    callback.onSuccess(books)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Error mencari buku: ${e.message}")
                }
            }
        }
    }

    // Metode untuk mengambil semua penulis
//    suspend fun getAllAuthors(): List<Author> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val snapshot = firestore.collection("authors").get().await()
//                val authors = snapshot.documents.mapNotNull { document ->
//                    try {
//                        Author(
//                            id = document.id,
//                            name = document.getString("name") ?: ""
//                            // Tambahkan field lain yang ada di Firestore
//                        )
//                    } catch (e: Exception) {
//                        Timber.tag("FIRESTORE_ERROR").e(e, "Error mengkonversi dokumen author")
//                        null
//                    }
//                }
//                Timber.tag("FIRESTORE_SUCCESS").d("Authors received from Firestore: ${authors.size}")
//                authors
//            } catch (e: Exception) {
//                Timber.tag("FIRESTORE_ERROR").e(e, "Error mengambil author dari Firestore")
//                emptyList()
//            }
//        }
//    }

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(errorMessage: String?)
    }
}