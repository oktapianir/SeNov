package com.okta.senov.API

import com.okta.senov.model.PostRequest
import com.okta.senov.model.PostResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface FakeStoreApiService {
    @POST("auth/login")
    suspend fun login(
        @Body loginRequest: PostRequest
    ): Response<PostResponse>
}