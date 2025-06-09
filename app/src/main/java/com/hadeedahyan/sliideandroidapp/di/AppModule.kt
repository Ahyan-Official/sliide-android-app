package com.hadeedahyan.sliideandroidapp.di


import android.content.Context
import android.util.Log
import com.hadeedahyan.sliideandroidapp.BuildConfig
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("OkHttp", message)
        }.apply { level = HttpLoggingInterceptor.Level.BODY }
        val apiKey = BuildConfig.API_KEY
        if (apiKey.isEmpty()) {
            Log.e("AppModule", "API_KEY is empty in BuildConfig")
        } else {
            Log.d("AppModule", "Using API_KEY: $apiKey")
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json")
                    .build()
                Log.d("Request", "URL: ${request.url}, Headers: ${request.headers}")
                chain.proceed(request)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://gorest.co.in/public/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService, @ApplicationContext context: Context): UserRepository = UserRepository(apiService,context)
}