package com.okta.senov.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.okta.senov.model.Book
import com.okta.senov.model.BookData
import com.okta.senov.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _popularBooks = MutableLiveData<List<BookData>>()
    val popularBooks: LiveData<List<BookData>> = _popularBooks

    private val _allBooks = MutableLiveData<List<Book>>()
    val allBooks: LiveData<List<Book>> = _allBooks

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchBooksFromApi(apiKey: String, query: String = "") {
        _loading.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bookDataList = bookRepository.fetchBooks(apiKey, query)

                _popularBooks.postValue(bookDataList.take(10))

                _allBooks.postValue(bookDataList.map { bookData ->
                    Book(
                        id = bookData.id,
                        title = bookData.title,
                        coverResourceId = bookData.image
                    )
                })

                Log.d("API_SUCCESS", "Books fetched successfully: ${bookDataList.size} books received.")
            } catch (e: Exception) {
                _errorMessage.postValue("Error fetching books: ${e.message}")
                Log.e("BookViewModel", "Error fetching books", e)
            } finally {
                _loading.postValue(false)
            }
        }
    }
}
