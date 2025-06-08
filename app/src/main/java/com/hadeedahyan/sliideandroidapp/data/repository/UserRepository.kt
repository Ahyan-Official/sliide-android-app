package com.hadeedahyan.sliideandroidapp.data.repository

import android.util.Log
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.domain.model.User
import java.util.regex.Pattern
import javax.inject.Inject


class UserRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUsersLastPage(): Result<List<User>> {
        return try {
            val initialResponse = apiService.getUsers(page = 1)
            if (!initialResponse.isSuccessful) {
                return Result.failure(UserFetchException("API error: ${initialResponse.code()}"))
            }
            val linkHeader = initialResponse.headers()["Link"] ?: return Result.success(initialResponse.body()?.map { it.toDomain(System.currentTimeMillis()) } ?: emptyList())

            val lastPage = extractLastPage(linkHeader) ?: return Result.success(initialResponse.body()?.map { it.toDomain(System.currentTimeMillis()) } ?: emptyList())
            val res = apiService.getUsers(page = lastPage)


            if (res.isSuccessful) {
                val users = res.body()?.map { it.toDomain(System.currentTimeMillis()) } ?: emptyList()
               // Log.d("UserRepository", "Fetched users: $users")
                Result.success(users)
            } else {
                Result.failure(UserFetchException("API error: ${res.code()}"))
                //te
            }

        } catch (e: Exception) {
            //Log.e("UserRepository", "Error: ${e.message}")
            Result.failure(UserFetchException("Network error: ${e.message}"))
        }
    }

    private fun extractLastPage(linkHeader: String): Int? {
        val pattern = Pattern.compile("<https://gorest\\.co\\.in/public/v2/users\\?page=(\\d+)>; rel=\"last\"")
        val matcher = pattern.matcher(linkHeader)

        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }

    private fun UserDto.toDomain(fetchTime: Long) = User(id, name, email, gender, status, fetchTime)
}

class UserFetchException(message: String) : Exception(message)