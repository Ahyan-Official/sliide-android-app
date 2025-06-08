package com.hadeedahyan.sliideandroidapp.data.repository

import android.util.Log
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


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

                Result.success(users)
            } else {
                Result.failure(UserFetchException("API error: ${res.code()}"))
                //te
            }

        } catch (e: Exception) {

            Result.failure(UserFetchException("Network error: ${e.message}"))
        }
    }


    suspend fun addUser(name: String, email: String): Result<User> = suspendCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userDto = UserDto(name = name, email = email, gender = "male", status = "active")
                val response = apiService.addUser(userDto)
                if (response.isSuccessful && response.code() == 201) {
                  //  Log.d("UserRepository", "User created: ${response.body()}")
                    val createdUserDto = response.body()
                    if (createdUserDto != null && createdUserDto.id != null) {
                        val fetchTime = System.currentTimeMillis() // Provide fetchTime here
                        val user = createdUserDto.toDomain(fetchTime)
                        continuation.resume(Result.success(user))
                    } else {
                        //Log.e("UserRepository", "No valid user data returned from API")
                        continuation.resume(Result.failure(UserFetchException("No valid user data returned")))
                    }
                } else {
                   // Log.e("UserRepository", "Failed to create user, code: ${response.code()}")
                    continuation.resume(Result.failure(UserFetchException("Creation failed: ${response.code()}")))
                }
            } catch (e: HttpException) {
              //  Log.e("UserRepository", "HTTP error creating user: ${e.message}")
                continuation.resume(Result.failure(UserFetchException("HTTP error: ${e.message}")))
            } catch (e: Exception) {
              //  Log.e("UserRepository", "Error creating user: ${e.message}")
                continuation.resume(Result.failure(UserFetchException("Network error: ${e.message}")))
            }
        }
    }


    private fun extractLastPage(linkHeader: String): Int? {
        val pattern = Pattern.compile("<https://gorest\\.co\\.in/public/v2/users\\?page=(\\d+)>; rel=\"last\"")
        val matcher = pattern.matcher(linkHeader)

        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }



    private fun UserDto.toDomain(fetchTime: Long) = User(id!!, name, email, gender, status, fetchTime)
}

class UserFetchException(message: String) : Exception(message)