package com.hadeedahyan.sliideandroidapp.presentation.screen

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.presentation.viewmodel.UserViewModel
import com.hadeedahyan.sliideandroidapp.utils.TimeUtils
@Composable
fun UserListScreen(modifier: Modifier = Modifier) {
    val viewModel: UserViewModel = hiltViewModel()
    val users by viewModel.uiState.collectAsState()

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    val statusBarHeight = GetStatusBarHeight()
    val navBarHeight = GetNavigationBarHeight()

    Box(modifier = modifier
        .fillMaxSize()
        .padding(top = statusBarHeight,bottom = navBarHeight),

        ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(16.dp)
            )
        } else {

            Column {
                LazyColumn(
                    contentPadding = PaddingValues( 10.dp, 10.dp,10.dp,10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(users.size) { index ->
                        UserCard(
                            user = users[index],
                            onLongPress = { user ->
                                userToDelete = user
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
            // Floating Action Button at bottom right
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp,10.dp)
            ) {
                Text("+")
            }
        }
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add New User") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { newName ->
                                name = newName
                                nameError = validateName(newName)
                            },
                            label = { Text("Name") },
                            isError = nameError != null,
                            supportingText = { nameError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { newEmail ->
                                email = newEmail
                                emailError = validateEmail(newEmail)
                            },
                            label = { Text("Email") },
                            isError = emailError != null,
                            supportingText = { emailError?.let { Text(it) } },
                            modifier = Modifier.fillMaxWidth().padding(8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        nameError = validateName(name)
                        emailError = validateEmail(email)
                        if (nameError == null && emailError == null) {
                            viewModel.addUser(name, email)
                            showDialog = false
                            name = ""
                            email = ""
                        } else {
                            Log.w("UserListScreen", "Validation failed: nameError=$nameError, emailError=$emailError")
                        }
                    },
                        enabled = nameError == null && emailError == null
                    ) {
                        Text("Add")
                    }
                }
            )
        }

        if (showDeleteDialog && userToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete User") },
                text = { Text("Delete ${userToDelete!!.name}?") },
                confirmButton = {
                    Button(onClick = {
                        viewModel.deleteUser(userToDelete!!.id)
                        Log.e("check", userToDelete!!.id.toString())
                        showDeleteDialog = false
                        userToDelete = null
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(user: User, onLongPress: (User) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress(user) }
                )
            },
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "Name: ${user.name}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = "Email: ${user.email}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text(
            text = "Status: ${user.status}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )
        Text(
            text = TimeUtils.formatRelativeTime(user.createdAt),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(8.dp)
        )
    }
}
@Composable
fun GetStatusBarHeight(): Dp {
    val insets = WindowInsets.statusBars
    val density = LocalDensity.current
    val statusBarHeightPx = insets.getTop(LocalDensity.current)
    return with(density) { statusBarHeightPx.toDp() }
}
@Composable
fun GetNavigationBarHeight(): Dp {
    val insets = WindowInsets.navigationBars
    val density = LocalDensity.current
    val navigationBarHeightPx = insets.getBottom(density)
    return with(density) { navigationBarHeightPx.toDp() }
}
// Validation functions
private fun validateName(name: String): String? {
    return when {
        name.isEmpty() -> "Name cannot be empty"
        name.length < 3 -> "Name must be at least 3 characters"
        else -> null
    }
}

private fun validateEmail(email: String): String? {
    return when {
        email.isEmpty() -> "Email cannot be empty"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Invalid email format"
        else -> null
    }
}