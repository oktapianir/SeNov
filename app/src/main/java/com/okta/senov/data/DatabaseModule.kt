package com.okta.senov.data.DatabaseModule

import android.app.Application
import android.content.Context
import com.okta.senov.API.BigBookApiService
import com.okta.senov.repository.BookRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.bigbookapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideBigBookApiService(retrofit: Retrofit): BigBookApiService {
        return retrofit.create(BigBookApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiRepository(
        bigBookApiService: BigBookApiService,
        context: Context
    ): BookRepository {
        return BookRepository(bigBookApiService, context)
    }

    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }
}
