package com.hadeedahyan.sliideandroidapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.domain.usecase.GetUsersUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.AddUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.DeleteUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase,
    private val addUserUseCase: AddUserUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<List<User>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        fetchUsers()
    }

    fun fetchUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            kotlinx.coroutines.delay(1000)
            val result = getUsersUseCase()
            _isLoading.value = false
            _uiState.value = if (result.isSuccess) result.getOrNull() ?: emptyList() else emptyList()
            _errorMessage.value = result.exceptionOrNull()?.message
            if (result.isSuccess) {
                _uiState.value = result.getOrNull() ?: emptyList()
                _errorMessage.value = null
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Unknown error"

            }
        }
    }

    fun addUser(name: String, email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = addUserUseCase(name, email)
            _isLoading.value = false

            if (result.isSuccess) {
                val newUser = result.getOrNull()
                newUser?.let { _uiState.value = _uiState.value + it }
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Add user failed"
            }
        }
    }

    fun deleteUser(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val updatedUsers = deleteUserUseCase(userId)
            _uiState.value = updatedUsers.ifEmpty { _uiState.value.filter { it.id != userId } }
            _isLoading.value = false
        }
    }

    private fun isRunningTest(): Boolean {
        return try {
            Class.forName("org.junit.Test")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}