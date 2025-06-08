package com.hadeedahyan.sliideandroidapp.presentation.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.hadeedahyan.sliideandroidapp.presentation.viewmodel.UserViewModel

@Composable
fun UserListScreen(modifier: Modifier = Modifier) {
    Text(text = "User List Screen", modifier = modifier)
    val viewModel: UserViewModel = hiltViewModel()
    val users = viewModel.uiState.collectAsState().value
    Text(text = "Users: $users", modifier = modifier)
}