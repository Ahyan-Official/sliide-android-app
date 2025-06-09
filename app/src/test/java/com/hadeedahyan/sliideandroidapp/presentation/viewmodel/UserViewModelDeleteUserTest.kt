package com.hadeedahyan.sliideandroidapp.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.hadeedahyan.sliideandroidapp.data.repository.UserFetchException
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.domain.usecase.AddUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.DeleteUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.GetUsersUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelDeleteUserTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val getUsersUseCase: GetUsersUseCase = mockk()
    private val addUserUseCase: AddUserUseCase = mockk()
    private val deleteUserUseCase: DeleteUserUseCase = mockk()
    private lateinit var viewModel: UserViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        // Mock getUsersUseCase for init block
        coEvery { getUsersUseCase.invoke() } returns Result.success(emptyList())
        // NEW: Mock addUserUseCase for setup
        coEvery { addUserUseCase("John Doe", "john@example.com") } returns Result.success(
            User(id = 1, name = "John Doe", email = "john@example.com", gender = "male", status = "active", createdAt = System.currentTimeMillis())
        )
        viewModel = UserViewModel(getUsersUseCase, addUserUseCase, deleteUserUseCase)
        // Initialize uiState with a test user
        viewModel.addUser("John Doe", "john@example.com")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteUser success removes user from uiState and clears error`() = runTest {
        // Arrange
        val userId = 1
        coEvery { deleteUserUseCase(userId) } returns emptyList() // Simulate successful deletion

        // Act
        viewModel.deleteUser(userId)

        // Assert
        val uiState = viewModel.uiState.first()
        val isLoading = viewModel.isLoading.first()
        val errorMessage = viewModel.errorMessage.first()

        assertTrue(uiState.isEmpty(), "uiState should be empty after deletion")
        assertFalse(isLoading, "isLoading should be false after operation")
        assertNull(errorMessage, "errorMessage should be null on success")
    }

    @Test
    fun `deleteUser failure retains uiState and clears error`() = runTest {
        // Arrange
        val userId = 1
        val initialUser = User(id = userId, name = "John Doe", email = "john@example.com", gender = "male", status = "active", createdAt = System.currentTimeMillis())
        coEvery { deleteUserUseCase(userId) } returns listOf(initialUser) // Simulate failure

        // Act
        viewModel.deleteUser(userId)

        // Assert
        val uiState = viewModel.uiState.first()
        val isLoading = viewModel.isLoading.first()
        val errorMessage = viewModel.errorMessage.first()

        assertTrue(uiState.contains(initialUser), "uiState should retain the user on failure")
        assertEquals(1, uiState.size, "uiState should contain exactly one user")
        assertFalse(isLoading, "isLoading should be false after operation")
        assertNull(errorMessage, "errorMessage should be null as no error is set")
    }
}