package com.hadeedahyan.sliideandroidapp.domain.usecase

import com.hadeedahyan.sliideandroidapp.data.repository.UserRepository
import com.hadeedahyan.sliideandroidapp.domain.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(name: String, email: String): Result<User> = suspendCoroutine { continuation ->
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val result = userRepository.addUser(name, email)
                continuation.resume(result)
            } catch (e: Exception) {
                continuation.resume(Result.failure(Exception("Use case error: ${e.message}")))
            }
        }
    }
}