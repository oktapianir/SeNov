package com.okta.senov.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.okta.senov.model.BookContent
import javax.inject.Inject
import com.okta.senov.model.Chapter


class BookContentRepository @Inject constructor() {
    private val db = FirebaseFirestore.getInstance()

//    suspend fun getBookContent(bookId: String): BookContent? {
//        return try {
//            val documentSnapshot = db.collection("bookContents")
//                .document(bookId)
//                .get()
//                .await()
//            Timber.d("Book ID: $bookId")
//
//            Timber.d("Firestore document: ${documentSnapshot.data}")
//
//            if (documentSnapshot.exists()) {
//                documentSnapshot.toObject(BookContent::class.java)
//            } else {
//                // Buku tidak ditemukan, buat konten placeholder
//                createPlaceholderContent(bookId)
//            }
//        } catch (e: Exception) {
//            Timber.e(e, "Error fetching book content for book $bookId")
//            null
//        }
//    }

    fun getBookContent(bookId: String, callback: (BookContent?, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("bookContents").document(bookId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val content = document.toObject(BookContent::class.java)
                    callback(content, null)
                } else {
                    callback(null, "Dokumen tidak ditemukan")
                }
            }
            .addOnFailureListener { e ->
                callback(null, e.message)
            }
    }


    // Membuat konten placeholder jika buku tidak memiliki konten
    private fun createPlaceholderContent(bookId: String): BookContent {
        return BookContent(
            bookId = bookId,
            title = "Cerita Contoh",
            chapters = listOf(
                Chapter(
                    number = 1,
                    title = "Bab 1: Pendahuluan",
                    content = "Cerita ini masih dalam proses pembuatan. Coba untuk baca cerita lain yaa"
                )
            )
        )
    }
}