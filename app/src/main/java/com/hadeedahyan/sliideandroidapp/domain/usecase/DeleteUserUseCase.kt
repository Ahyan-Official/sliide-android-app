package com.hadeedahyan.sliideandroidapp.domain.usecase

import com.hadeedahyan.sliideandroidapp.data.repository.UserRepository
import com.hadeedahyan.sliideandroidapp.domain.model.User
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: Int): List<User> {
        //userRepository.createdAtMap.remove(userId)
        userRepository.deleteUser(userId) // Use repository method
        return emptyList() // UI will filter based on this
    }
}