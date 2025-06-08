package com.hadeedahyan.sliideandroidapp.data.repository

import android.content.Context
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.domain.usecase.AddUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.GetUsersUseCase
import com.hadeedahyan.sliideandroidapp.presentation.viewmodel.UserViewModel
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Assertions.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.util.Date
import okhttp3.Response as OkHttpResponse

@ExperimentalCoroutinesApi
class UserRepositoryTest {
    private lateinit var viewModel: UserViewModel
    private val apiService: ApiService = mock()
    private val context: Context = mockk(relaxed = true)
    private val repository = UserRepository(apiService,context)
    private val fetchTime = Date().time
    private val addUserUseCase: AddUserUseCase = mock()
    private val getUsersUseCase: GetUsersUseCase = mock()
    private val testDispatcher = StandardTestDispatcher()
    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = UserViewModel(getUsersUseCase, addUserUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUsersLastPage returns success with users from last page`() = runTest {
        val usersDto = listOf(UserDto(1, "John Doe", "john@example.com", "male", "active"))
        val linkHeader = "<https://gorest.co.in/public/v2/users?page=2>; rel=\"last\""
        val mediaType = "application/json".toMediaType()
        val responseBody = ResponseBody.create(mediaType, """[]""")
        val okHttpResponse = OkHttpResponse.Builder()
            .request(Request.Builder().url("https://gorest.co.in/public/v2/users?page=1").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .addHeader("Link", linkHeader)
            .build()
        whenever(apiService.getUsers(1)).thenReturn(Response.success(usersDto, okHttpResponse))
        whenever(apiService.getUsers(2)).thenReturn(Response.success(usersDto))

        val result = repository.getUsersLastPage()

        assertTrue(result.isSuccess)
        val expectedUsers = usersDto.map { User(it.id!!, it.name, it.email, it.gender, it.status, fetchTime) }
        assertEquals(expectedUsers.map { it.copy(createdAt = null) }, result.getOrNull()?.map { it.copy(createdAt = null) })
    }

    @Test
    fun `getUsersLastPage returns failure on initial API error`() = runTest {
        whenever(apiService.getUsers(1)).thenReturn(Response.error(500, ResponseBody.create("application/json".toMediaType(), "")))

        val result = repository.getUsersLastPage()

        assertTrue(result.isFailure)
        assertEquals("API error: 500", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsersLastPage returns failure on last page API error`() = runTest {
        val usersDto = listOf(UserDto(1, "John Doe", "john@example.com", "male", "active"))
        val linkHeader = "<https://gorest.co.in/public/v2/users?page=2>; rel=\"last\""
        val mediaType = "application/json".toMediaType()
        val responseBody = ResponseBody.create(mediaType, """[]""")
        val okHttpResponse = OkHttpResponse.Builder()
            .request(Request.Builder().url("https://gorest.co.in/public/v2/users?page=1").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .addHeader("Link", linkHeader)
            .build()
        whenever(apiService.getUsers(1)).thenReturn(Response.success(usersDto, okHttpResponse))
        whenever(apiService.getUsers(2)).thenReturn(Response.error(404, ResponseBody.create("application/json".toMediaType(), "")))

        val result = repository.getUsersLastPage()

        assertTrue(result.isFailure)
        assertEquals("API error: 404", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsersLastPage returns failure on network error`() = runTest {
        whenever(apiService.getUsers(1)).thenThrow(RuntimeException("Network failure"))

        val result = repository.getUsersLastPage()

        assertTrue(result.isFailure)
        assertEquals("Network error: Network failure", result.exceptionOrNull()?.message)
    }

    @Test
    fun `getUsersLastPage returns success with no link header`() = runTest {
        val usersDto = listOf(UserDto(1, "John Doe", "john@example.com", "male", "active"))
        val mediaType = "application/json".toMediaType()
        val responseBody = ResponseBody.create(mediaType, """[]""")
        val okHttpResponse = OkHttpResponse.Builder()
            .request(Request.Builder().url("https://gorest.co.in/public/v2/users?page=1").build())
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)
            .build()
        whenever(apiService.getUsers(1)).thenReturn(Response.success(usersDto, okHttpResponse))

        val result = repository.getUsersLastPage()

        assertTrue(result.isSuccess)
        val expectedUsers = usersDto.map { User(it.id!!, it.name, it.email, it.gender, it.status, fetchTime) }
        assertEquals(expectedUsers.map { it.copy(createdAt = null) }, result.getOrNull()?.map { it.copy(createdAt = null) })
    }





}