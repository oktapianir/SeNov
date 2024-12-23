package com.okta.senov.repository

import android.content.Context
import android.util.Log
import com.okta.senov.API.BigBookApiService
import com.okta.senov.model.BookData
import com.okta.senov.model.BookResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val bigBookApiService: BigBookApiService,
    private val context: Context
) {

    suspend fun fetchBooks(apiKey: String, query: String): List<BookData> {
        val response = bigBookApiService.getSearchBooks(query, apiKey)
        if (response.isSuccessful) {
            val books = response.body()?.books?.flatten() ?: emptyList()
            Log.d("API_RESPONSE", "Books received: ${books.size}")
            return books.map { book ->
                BookData(
                    id = book.id,
                    title = book.title,
                    image = book.image
                )
            }
        } else {
            Log.e("API_ERROR", "Error response: ${response.message()}")
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

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(errorMessage: String?)
    }
}
