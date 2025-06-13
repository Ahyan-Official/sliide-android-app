package com.hadeedahyan.sliideandroidapp.presentation.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hadeedahyan.sliideandroidapp.R
import com.hadeedahyan.sliideandroidapp.domain.model.User
import com.hadeedahyan.sliideandroidapp.presentation.viewmodel.UserViewModel
import com.hadeedahyan.sliideandroidapp.utils.TimeUtils

// Define color palette
val PureWhite = Color(0xFFFFFFFF)
val PurePurple = Color(0xFF800080)
val GlassBackground = Color.White.copy(alpha = 0.2f)
val GlassBorder = Color.White.copy(alpha = 0.5f)
val PureBlack = Color(0xFF000000)
val PureWhiteText = Color(0xFFFFFFFF)

// Placeholder futuristic font (replace with Orbitron or similar)
val FuturisticFont = FontFamily(
    Font(R.font.sansr, FontWeight.Normal),
    Font(R.font.sansr, FontWeight.Bold)
)

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PureWhiteText)
            .padding(top = statusBarHeight, bottom = navBarHeight)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = PurePurple,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .testTag("LoadingIndicator")
            )
        } else if (errorMessage != null) {
            Text(
                text = "Error: $errorMessage",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FuturisticFont,
                    color = Color.Black,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .shadow(6.dp, CircleShape)
                    .testTag("AddFab"),
                shape = CircleShape,
                containerColor = PurePurple,
                contentColor = PureWhiteText
            ) {
                Text(
                    text = "+",
                    fontSize = 24.sp,
                    fontFamily = FuturisticFont,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (showDialog) {
            AddUserDialog(
                name = name,
                email = email,
                nameError = nameError,
                emailError = emailError,
                onNameChange = { newName ->
                    name = newName
                    nameError = validateName(newName)
                },
                onEmailChange = { newEmail ->
                    email = newEmail
                    emailError = validateEmail(newEmail)
                },
                onConfirm = {
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
                onDismiss = { showDialog = false }
            )
        }
        if (showDeleteDialog && userToDelete != null) {
            DeleteUserDialog(
                user = userToDelete!!,
                onConfirm = {
                    viewModel.deleteUser(userToDelete!!.id)
                    showDeleteDialog = false
                    userToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    userToDelete = null
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
            .clip(RoundedCornerShape(16.dp))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(PureBlack)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress(user) })
            }
            .testTag("UserCard_${user.id}"),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureBlack)
                .padding(16.dp)
        ) {
            Text(
                text = "Name: ${user.name}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FuturisticFont,
                    color = PureWhiteText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Email: ${user.email}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FuturisticFont,
                    color = PureWhiteText,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = "Status: ${user.status}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FuturisticFont,
                    color = PureWhiteText,
                    fontSize = 14.sp
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                text = TimeUtils.formatRelativeTime(user.createdAt),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FuturisticFont,
                    color = PureWhiteText,
                    fontSize = 12.sp
                )
            )
        }
    }
}

@Composable
fun AddUserDialog(
    name: String,
    email: String,
    nameError: String?,
    emailError: String?,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite),
        title = {
            Text(
                "Add New User",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FuturisticFont,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Name", color = Color.Black) },
                    isError = nameError != null,
                    supportingText = { nameError?.let { Text(it, color = Color.Black) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("NameField"),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurePurple,
                        unfocusedBorderColor = PureBlack,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text("Email", color = Color.Black) },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it, color = Color.Black) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .testTag("EmailField"),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PurePurple,
                        unfocusedBorderColor = PureBlack,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = nameError == null && emailError == null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurePurple,
                    contentColor = PureWhiteText
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp))
            ) {
                Text(
                    "Add",
                    fontFamily = FuturisticFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = PureWhiteText
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Cancel",
                    fontFamily = FuturisticFont
                )
            }
        }
    )
}

@Composable
fun DeleteUserDialog(
    user: User,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(PureWhite),
        title = {
            Text(
                "Delete User",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FuturisticFont,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        },
        text = {
            Text(
                "Delete ${user.name}?",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FuturisticFont,
                    color = Color.Black,
                    fontSize = 16.sp
                )
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PurePurple,
                    contentColor = PureWhiteText
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.shadow(2.dp, RoundedCornerShape(8.dp))
            ) {
                Text(
                    "OK",
                    fontFamily = FuturisticFont,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = PureWhiteText
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Cancel",
                    fontFamily = FuturisticFont
                )
            }
        }
    )
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
