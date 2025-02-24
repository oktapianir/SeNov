package com.okta.senov.repository

import com.okta.senov.API.FakeStoreApiService
import com.okta.senov.model.PostRequest
import com.okta.senov.model.PostResponse
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val fakeStoreApiService: FakeStoreApiService
) {
    suspend fun login(username: String, password: String): Result<PostResponse> {
        Timber.tag("AuthRepository").d("Login attempt with username: $username and password: $password")
        return try {
            val response = fakeStoreApiService.login(PostRequest(username, password))
            Timber.tag("AuthRepository").d("Login response: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                Timber.tag("AuthRepository").d("Login successful, received token: ${response.body()?.token}")
                Result.success(response.body()!!)
            } else {
                Timber.tag("AuthRepository").e("Login failed: ${response.code()} - ${response.message()}")
                Result.failure(Exception("Login failed with error code: ${response.code()}"))
            }
        } catch (e: IOException) {
            Timber.tag("AuthRepository").e("Login error: Network issue or timeout")
            Result.failure(Exception("Login failed due to network issue or timeout"))
        } catch (e: Exception) {
            Timber.tag("AuthRepository").e("Login error: ${e.message}")
            Result.failure(e)
        }
    }
}
