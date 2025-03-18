package com.okta.senov.data.repository

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

//    @Provides
//    @Singleton
//    fun provideApiRepository(
//        bigBookApiService: BigBookApiService,
//        context: Context
//    ): BookRepository {
//        return BookRepository(bigBookApiService, context)
//    }
}
