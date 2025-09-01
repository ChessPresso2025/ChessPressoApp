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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.chesspresso.auth.presentation.AuthViewModel

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
            Text(
                text = "Profil",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Meine Informationen",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    val userProfileState = viewModel.userProfileState.collectAsState().value
                    when (userProfileState) {
                        is UserProfileUiState.Loading -> CircularProgressIndicator()
                        is UserProfileUiState.Error -> Text(
                            "Fehler: " + userProfileState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        is UserProfileUiState.Success -> {
                            val profile = userProfileState.profile
                            Text("Name: ${profile.username}", style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("E-Mail: ${profile.email}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Username Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Benutzernamen ändern",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Neuer Benutzername") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showUsernameConfirmDialog = true },
                        enabled = usernameChangeState !is UsernameChangeState.Loading && newUsername.length in 3..32,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Benutzernamen ändern")
                    }
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
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Passwort ändern",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text("Altes Passwort") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Neues Passwort") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showPasswordConfirmDialog = true },
                        enabled = passwordChangeState !is PasswordChangeState.Loading && oldPassword.length >= 4 && newPassword.length in 4..64,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Passwort ändern")
                    }
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
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }

                        else -> {}
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )

            ) {
                Text(
                    text = "Abmelden",
                    color = Color.White
                )
            }

        }
    }


    if (showUsernameConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUsernameConfirmDialog = false },
            title = { Text("Benutzernamen ändern") },
            text = { Text("Um den Benutzernamen zu ändern, musst du dich neu anmelden. Bist du sicher, dass du fortfahren möchtest?") },
            confirmButton = {
                TextButton(onClick = {
                    showUsernameConfirmDialog = false
                    viewModel.changeUsername(newUsername)
                }) { Text("Ja, ändern") }
            },
            dismissButton = {
                TextButton(onClick = { showUsernameConfirmDialog = false }) { Text("Abbrechen") }
            }
        )
    }

    if (showPasswordConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordConfirmDialog = false },
            title = { Text("Passwort ändern") },
            text = { Text("Um das Passwort zu ändern, musst du dich neu anmelden. Bist du sicher, dass du fortfahren möchtest?") },
            confirmButton = {
                TextButton(onClick = {
                    showPasswordConfirmDialog = false
                    viewModel.changePassword(oldPassword, newPassword)
                }) { Text("Ja, ändern") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordConfirmDialog = false }) { Text("Abbrechen") }
            }
        )
    }
}
