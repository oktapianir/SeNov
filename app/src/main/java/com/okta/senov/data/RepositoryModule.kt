package com.okta.senov.data.repository

import android.content.Context
import com.okta.senov.API.BigBookApiService
import com.okta.senov.repository.BookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideApiRepository(
        bigBookApiService: BigBookApiService,
        context: Context
    ): BookRepository {
        return BookRepository(bigBookApiService, context)
    }
}
