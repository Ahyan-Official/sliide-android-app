package com.hadeedahyan.sliideandroidapp.presentation.viewmodel

import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.domain.usecase.AddUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.DeleteUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.GetUsersUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class UserViewModelTest {

    private lateinit var viewModel: UserViewModel
    private val getUsersUseCase: GetUsersUseCase = mock()
    private val addUserUseCase: AddUserUseCase = mock()
    private val deleteUserUseCase: DeleteUserUseCase = mock()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserViewModel(getUsersUseCase,addUserUseCase,deleteUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `fetchUsers sets loading state and updates uiState on success`() = runTest {
        // Arrange
        val users = listOf(User(1, "John Doe", "john@example.com", "male", "active", createdAt = System.currentTimeMillis()))
        whenever(getUsersUseCase.invoke()).thenReturn(Result.success(users))

        // Act
        viewModel.fetchUsers()
        // Increased delay to capture isLoading true state
        testDispatcher.scheduler.advanceTimeBy(1000) // Increased from 1 to 50 for reliability
        assertEquals(true, viewModel.isLoading.first()) // Check loading state
        testDispatcher.scheduler.advanceUntilIdle() // Complete the coroutine

        // Assert
        assertEquals(false, viewModel.isLoading.first())
        assertEquals(users, viewModel.uiState.first())
        assertNull(viewModel.errorMessage.first())
    }

    @Test
    fun `fetchUsers sets loading state and errorMessage on failure`() = runTest {
        // Arrange
        val exception = Exception("Network error")
        whenever(getUsersUseCase.invoke()).thenReturn(Result.failure(exception))

        // Act
        viewModel.fetchUsers()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(false, viewModel.isLoading.first())
        assertEquals(emptyList<User>(), viewModel.uiState.first())
        assertEquals("Network error", viewModel.errorMessage.first())
    }


}