package com.hadeedahyan.sliideandroidapp.data.remote

import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("users")
    suspend fun getUsers(@Query("page") page: Int, @Query("per_page") perPage: Int = 20): Response<List<UserDto>>
}