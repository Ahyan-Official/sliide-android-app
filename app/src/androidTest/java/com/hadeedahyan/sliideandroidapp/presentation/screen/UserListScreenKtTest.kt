package com.hadeedahyan.sliideandroidapp.presentation.screen

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performGesture
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.action.ViewActions.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hadeedahyan.sliideandroidapp.data.remote.dto.UserDto
import com.hadeedahyan.sliideandroidapp.di.AppModule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Singleton
import com.hadeedahyan.sliideandroidapp.data.remote.ApiService
import com.hadeedahyan.sliideandroidapp.data.repository.UserRepository
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.domain.usecase.AddUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.DeleteUserUseCase
import com.hadeedahyan.sliideandroidapp.domain.usecase.GetUsersUseCase
import io.mockk.coEvery
import io.mockk.mockk
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@UninstallModules(AppModule::class)
class UserListScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeTestRule.setContent {
            UserListScreen()
        }
        composeTestRule.waitForIdle() // Ensure initial composition
    }

    @Test
    fun testInitialLoadingIndicatorDisplayed() {
        composeTestRule.onNodeWithTag("LoadingIndicator").assertIsDisplayed()
    }

    @Test
    fun testFabClickOpensAddDialog() {
        composeTestRule.onNodeWithTag("AddFab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Add New User").assertIsDisplayed()
    }

    @Test
    fun testAddUserWithValidInput() {
        composeTestRule.onNodeWithTag("AddFab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("NameField").performTextInput("John")
        composeTestRule.onNodeWithTag("EmailField").performTextInput("john@example.com")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Name: John").assertIsDisplayed()
    }

    @Test
    fun testAddUserWithInvalidInputShowsError() {
        composeTestRule.onNodeWithTag("AddFab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("NameField").performTextInput("J")
        composeTestRule.onNodeWithTag("EmailField").performTextInput("invalid-email")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Name must be at least 3 characters").assertIsDisplayed()
    }

    @Test
    fun testUserCardLongPressOpensDeleteDialog() {
        composeTestRule.onNodeWithTag("AddFab").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("NameField").performTextInput("John")
        composeTestRule.onNodeWithTag("EmailField").performTextInput("john@example.com")
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Name: John").performGesture { longClick() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete User").assertIsDisplayed()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TestAppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        val mockApi = mockk<ApiService>(relaxed = true)
        coEvery { mockApi.getUsers(1) } returns Response.success(listOf(
            UserDto(1, "Test User", "test@example.com", "male", "active")
        ))
        return mockApi
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        val mockRepo = mockk<UserRepository>(relaxed = true)
        coEvery { mockRepo.getUsersLastPage() } returns Result.success(listOf(
            User(1, "Test User", "test@example.com", "male", "active", System.currentTimeMillis())
        ))
        coEvery { mockRepo.addUser(any(), any()) } returns Result.success(
            User(2, "Added User", "added@example.com", "female", "active", System.currentTimeMillis())
        )
        coEvery { mockRepo.deleteUser(any()) } returns Result.success(Unit)
        return mockRepo
    }

    @Provides
    @Singleton
    fun provideGetUsersUseCase(repository: UserRepository): GetUsersUseCase {
        return GetUsersUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideAddUserUseCase(repository: UserRepository): AddUserUseCase {
        return AddUserUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideDeleteUserUseCase(repository: UserRepository): DeleteUserUseCase {
        return DeleteUserUseCase(repository)
    }
}