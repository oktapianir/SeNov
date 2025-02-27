package com.okta.senov.API

import com.okta.senov.model.AuthorResponse
import com.okta.senov.model.BookResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BigBookApiService {

    @GET("search-books")
    suspend fun getSearchBooks(
        @Query("query") query: String,
        @Query("api-key") apiKey: String
    ): Response<BookResponse>

    @GET("search_author")
    suspend fun getSearchAuthors(
        @Query("author") author: String,
        @Query("api-key") apiKey: String
    ): Response<AuthorResponse>
}