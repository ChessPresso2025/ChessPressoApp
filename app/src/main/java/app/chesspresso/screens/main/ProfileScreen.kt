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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.ui.theme.CoffeeButton
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText
import app.chesspresso.ui.theme.CoffeeText
import app.chesspresso.ui.theme.CoffeeTextField

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
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }

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
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CoffeeHeadlineText(
                text = "Mein Profil"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Info Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Change Username Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth(),
                content = {
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
                        CoffeeTextField(
                            value = newUsername,
                            onValueChange = { newUsername = it },
                            label = "Neuer Benutzername",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Benutzername") }
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
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Change Password Card
            CoffeeCard(
                modifier = Modifier.fillMaxWidth(),
                content = {
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
                        CoffeeTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = "Altes Passwort",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (oldPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Altes Passwort") },
                            trailingIcon = {
                                val image = if (oldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                val desc = if (oldPasswordVisible) "Passwort verbergen" else "Passwort anzeigen"
                                IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                                    Icon(image, contentDescription = desc)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        CoffeeTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = "Neues Passwort",
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (newPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Neues Passwort") },
                            trailingIcon = {
                                val image = if (newPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility
                                val desc = if (newPasswordVisible) "Passwort verbergen" else "Passwort anzeigen"
                                IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                                    Icon(image, contentDescription = desc)
                                }
                            }
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
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

        }

        CoffeeButton(
            onClick = onLogout,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            error = true,
            content = {
                Text("Abmelden")
            }
        )
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
