package com.hadeedahyan.sliideandroidapp.data.repository

import android.content.Context
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.HttpException
import retrofit2.Response
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class UserRepositoryDeleteUserTest {

    private lateinit var repository: UserRepository
    private val apiService: ApiService = mock()
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = UserRepository(apiService, context)
        // Initialize createdAtMap with a test entry
        repository.createdAtMap[1] = System.currentTimeMillis()
    }

    @After
    fun tearDown() {
        testScope.cleanupTestCoroutines()
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteUser success returns Result success and removes user from createdAtMap`() = testScope.runTest {
        // Arrange
        val userId = 1
        val response = Response.success<Unit>(204, Unit)
        whenever(apiService.deleteUser(userId)).thenReturn(response)

        // Act
        val result = repository.deleteUser(userId)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(Unit, result.getOrNull())
        assertTrue(repository.createdAtMap.isEmpty(), "createdAtMap should be empty after deletion")
    }

    @Test
    fun `deleteUser HTTP error returns Result failure with UserFetchException`() = testScope.runTest {
        // Arrange
        val userId = 1
        val mediaType = "application/json".toMediaType()
        val responseBody = ResponseBody.create(mediaType, """{"error":"Not Found"}""")
        val httpException = HttpException(Response.error<Unit>(404, responseBody))
        whenever(apiService.deleteUser(userId)).thenThrow(httpException)

        // Act
        val result = repository.deleteUser(userId)

        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is UserFetchException)
        assertEquals("HTTP error: ${httpException.message}", exception?.message)
        assertTrue(repository.createdAtMap.containsKey(userId), "createdAtMap should retain userId on failure")
    }
}