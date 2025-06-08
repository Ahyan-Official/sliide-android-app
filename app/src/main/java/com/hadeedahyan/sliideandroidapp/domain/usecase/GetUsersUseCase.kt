package com.hadeedahyan.sliideandroidapp.domain.usecase

import com.hadeedahyan.sliideandroidapp.data.repository.UserRepository
import com.hadeedahyan.sliideandroidapp.domain.model.User
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
private val repository: UserRepository
) {
    suspend operator fun invoke(): Result<List<User>> {
        return repository.getUsersLastPage()
    }
}