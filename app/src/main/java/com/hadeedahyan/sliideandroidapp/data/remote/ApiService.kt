package com.hadeedahyan.sliideandroidapp.data.remote

import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("users")
    suspend fun getUsers(@Query("page") page: Int, @Query("per_page") perPage: Int = 20): Response<List<UserDto>>

    @POST("users")
    suspend fun addUser(@Body user: UserDto): Response<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") userId: Int): Response<Unit>
}