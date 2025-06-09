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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue


@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelAddUserTest {

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
        // Mock getUsersUseCase to return an empty list to handle init block
        coEvery { getUsersUseCase.invoke() } returns Result.success(emptyList())
        viewModel = UserViewModel(getUsersUseCase, addUserUseCase,deleteUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addUser success updates uiState with new user and clears error`() = runTest {
        // Arrange
        val newUser = User(
            id = 1,
            name = "John Doe",
            email = "john@example.com",
            gender = "male",
            status = "active",
            createdAt = System.currentTimeMillis()
        )
        coEvery { addUserUseCase("John Doe", "john@example.com") } returns Result.success(newUser)

        // Act
        viewModel.addUser("John Doe", "john@example.com")

        // Assert
        val uiState = viewModel.uiState.first()
        val isLoading = viewModel.isLoading.first()
        val errorMessage = viewModel.errorMessage.first()

        assertTrue(uiState.contains(newUser), "New user should be added to uiState")
        assertEquals(1, uiState.size, "uiState should contain exactly one user")
        assertEquals(false, isLoading, "isLoading should be false after operation")
        assertNull(errorMessage, "errorMessage should be null on success")
    }

    @Test
    fun `addUser failure sets error message and does not update uiState`() = runTest {
        // Arrange
        val error = UserFetchException("Failed to add user: 400")
        coEvery { addUserUseCase("John Doe", "john@example.com") } returns Result.failure(error)

        // Act
        viewModel.addUser("John Doe", "john@example.com")

        // Assert
        val uiState = viewModel.uiState.first()
        val isLoading = viewModel.isLoading.first()
        val errorMessage = viewModel.errorMessage.first()

        assertTrue(uiState.isEmpty(), "uiState should remain empty on failure")
        assertEquals(false, isLoading, "isLoading should be false after operation")
        assertEquals(error.message, errorMessage, "errorMessage should match the exception message")
    }
}