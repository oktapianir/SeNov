package com.okta.senov.repository

import android.content.Context
import com.okta.senov.API.BigBookApiService
import com.okta.senov.model.Author
import com.okta.senov.model.BookData
import com.okta.senov.model.BookResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bigBookApiService: BigBookApiService,
    private val context: Context
) {

    suspend fun fetchBooks(apiKey: String, query: String): List<BookData> {
        val response = bigBookApiService.getSearchBooks(query, apiKey)
        if (response.isSuccessful) {
            val books = response.body()?.books?.flatten() ?: emptyList()
            Timber.tag("API_RESPONSE").d("Books received: ${books.size}")
            return books.map { book ->
                BookData(
                    id = book.id,
                    title = book.title,
//                    authorName = book,authorName,
                    image = book.image
                )
            }
        } else {
            Timber.tag("API_ERROR").d("Error response: ${response.message()}")
            throw Exception("Error fetching books: ${response.code()}")
        }
    }

    fun fetchSearchBooks(apiKey: String, query: String, callback: ApiCallback<BookResponse>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = bigBookApiService.getSearchBooks(query, apiKey)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        callback.onSuccess(response.body()!!)
                    } else {
                        callback.onError("Failed to search books: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Error: ${e.message}")
                }
            }
        }
    }
    suspend fun fetchAuthors(apiKey: String, authorName: String): List<Author> {
        return withContext(Dispatchers.IO) {
            try {
                val response = bigBookApiService.getSearchAuthors(authorName, apiKey)
                if (response.isSuccessful) {
                    response.body()?.authors ?: emptyList()
                } else {
                    Timber.tag("API_ERROR").e("Failed to fetch authors: ${response.message()}")
                    emptyList()
                }
            } catch (e: Exception) {
                Timber.tag("API_ERROR").e("Exception fetching authors: ${e.message}")
                emptyList()
            }
        }
    }

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(errorMessage: String?)
    }
}
