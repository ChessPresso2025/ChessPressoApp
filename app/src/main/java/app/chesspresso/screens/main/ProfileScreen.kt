package app.chesspresso.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeText

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    outerNavController: NavHostController
) {
    val usernameChangeState by viewModel.usernameChangeState.collectAsState()
    val passwordChangeState by viewModel.passwordChangeState.collectAsState()
    var newUsername by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showUsernameConfirmDialog by remember { mutableStateOf(false) }
    var showPasswordConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(usernameChangeState) {
        if (usernameChangeState is UsernameChangeState.Success) {
            newUsername = "" // Clear input after success
            // Navigation is handled by the event below
        }
    }

    LaunchedEffect(passwordChangeState) {
        if (passwordChangeState is PasswordChangeState.Success) {
            oldPassword = ""
            newPassword = ""
            // Navigation is handled by the event below
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.LogoutAndNavigateToLogin -> {
                    authViewModel.logout()
                    outerNavController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp), // Extra padding at the bottom if content is long
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoffeeText(
                text = "Profil",
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Info Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Meine Informationen",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val userProfileState = viewModel.userProfileState.collectAsState().value
                    when (userProfileState) {
                        is UserProfileUiState.Loading -> CircularProgressIndicator()
                        is UserProfileUiState.Error -> CoffeeText(
                            "Fehler: " + userProfileState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        is UserProfileUiState.Success -> {
                            val profile = userProfileState.profile
                            CoffeeText("Name: ${profile.username}")
                            Spacer(modifier = Modifier.height(8.dp))
                            CoffeeText("E-Mail: ${profile.email}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Username Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Benutzernamen ändern",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { CoffeeText("Neuer Benutzername") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CoffeeButton(
                        onClick = { showUsernameConfirmDialog = true },
                        enabled = usernameChangeState !is UsernameChangeState.Loading && newUsername.length in 3..32,
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text("Benutzernamen ändern")
                        }
                    )
                    when (val state = usernameChangeState) {
                        is UsernameChangeState.Loading -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }
                        is UsernameChangeState.Success -> {
                            // Message handled by logout navigation
                        }
                        is UsernameChangeState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CoffeeText(state.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CoffeeText(
                        text = "Passwort ändern",
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { CoffeeText("Altes Passwort") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { CoffeeText("Neues Passwort") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CoffeeButton(
                        onClick = { showPasswordConfirmDialog = true },
                        enabled = passwordChangeState !is PasswordChangeState.Loading && oldPassword.length >= 4 && newPassword.length in 4..64,
                        modifier = Modifier.fillMaxWidth(),
                        content = {
                            Text("Passwort ändern")
                        }
                    )
                    when (val state = passwordChangeState) {
                        is PasswordChangeState.Loading -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator()
                        }
                        is PasswordChangeState.Success -> {
                            // Message handled by logout navigation
                        }
                        is PasswordChangeState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            CoffeeText(state.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CoffeeButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                content = {
                    Text("Abmelden")
                }
            )

        }
    }


    if (showUsernameConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUsernameConfirmDialog = false },
            title = { CoffeeText("Benutzernamen ändern") },
            text = { CoffeeText("Um den Benutzernamen zu ändern, musst du dich neu anmelden. Bist du sicher, dass du fortfahren möchtest?") },
            confirmButton = {
                TextButton(onClick = {
                    showUsernameConfirmDialog = false
                    viewModel.changeUsername(newUsername)
                }) { CoffeeText("Ja, ändern") }
            },
            dismissButton = {
                TextButton(onClick = { showUsernameConfirmDialog = false }) { CoffeeText("Abbrechen") }
            }
        )
    }

    if (showPasswordConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordConfirmDialog = false },
            title = { CoffeeText("Passwort ändern") },
            text = { CoffeeText("Um das Passwort zu ändern, musst du dich neu anmelden. Bist du sicher, dass du fortfahren möchtest?") },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordConfirmDialog = false
                    viewModel.changePassword(oldPassword, newPassword)
                }) { CoffeeText("Ja, ändern") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordConfirmDialog = false }) { CoffeeText("Abbrechen") }
            }
        )
    }
}
