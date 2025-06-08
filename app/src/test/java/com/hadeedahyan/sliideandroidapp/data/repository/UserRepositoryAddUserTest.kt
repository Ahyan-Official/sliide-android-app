package com.hadeedahyan.sliideandroidapp.data.repository

import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher

import kotlinx.coroutines.test.runTest

import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import org.junit.jupiter.api.Assertions.assertEquals

import org.mockito.kotlin.any

import org.junit.Assert.assertTrue

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody


import java.util.Date
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import org.junit.After





@ExperimentalCoroutinesApi
class UserRepositoryAddUserTest {

    private lateinit var repository: UserRepository
    private val apiService: ApiService = mock()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val fetchTime = Date().time

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = UserRepository(apiService)
    }

    @After
    fun tearDown() {
        testScope.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun `addUser success returns Result with created user`() = testScope.runTest {
        // Arrange
        val userDto = UserDto(
            id = 1,
            name = "John Doe",
            email = "john@example.com",
            gender = "male",
            status = "active"
        )
        val response = Response.success(201, userDto)
        whenever(apiService.addUser(any<UserDto>())).thenReturn(response)

        // Act
        val result = repository.addUser("John Doe", "john@example.com")

        // Assert
        assertTrue(result.isSuccess)
        val user = result.getOrNull()
        val expectedUser = User(1, "John Doe", "john@example.com", "male", "active", fetchTime)
        assertEquals(expectedUser.copy(createdAt = null), user?.copy(createdAt = null))
        assertTrue(user?.createdAt != null)
    }

    @Test
    fun `addUser HTTP error returns Result failure with UserFetchException`() = testScope.runTest {
        // Arrange
        val mediaType = "application/json".toMediaType()
        val responseBody = ResponseBody.create(mediaType, """{"error":"Bad Request"}""")
        val httpException = HttpException(Response.error<UserDto>(400, responseBody))
        whenever(apiService.addUser(any<UserDto>())).thenThrow(httpException)

        // Act
        val result = repository.addUser("John Doe", "john@example.com")

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is UserFetchException)
        assertEquals("HTTP error: ${httpException.message}", exception?.message)
    }
}