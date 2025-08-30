package app.chesspresso.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState by authViewModel.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var isRegistering by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Vorausgefüllter Benutzername, falls vorhanden
    val storedUsername = remember { authViewModel.getStoredUsername() }
    if (username.isEmpty() && storedUsername != null) {
        username = storedUsername
    }

    // Navigation nach erfolgreichem Login
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Snackbar für Fehler
    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Error -> {
                snackbarHostState.showSnackbar(state.message)
            }

            else -> {}
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = if (isRegistering) "Registrierung" else "Anmeldung",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading
                )

                if (isRegistering) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-Mail") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = authState !is AuthState.Loading
                    )
                }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (isRegistering) {
                            if (username.isNotBlank() && password.isNotBlank() && email.isNotBlank()) {
                                authViewModel.register(username.trim(), password, email.trim())
                            } else {
                                authViewModel.setErrorMessage("Alle Felder müssen ausgefüllt werden")
                            }
                        } else {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                authViewModel.login(username.trim(), password)
                            } else {
                                authViewModel.setErrorMessage("Benutzername und Passwort müssen ausgefüllt werden")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(if (isRegistering) "Registrieren" else "Anmelden")
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegistering) "Bereits ein Konto?" else "Noch kein Konto?",
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        modifier = Modifier.padding(top = 8.dp),
                        onClick = {
                            isRegistering = !isRegistering
                            email = ""
                        },
                        enabled = authState !is AuthState.Loading
                    ) {
                        Text(if (isRegistering) "Anmelden" else "Registrieren")
                    }
                }

                when (val state = authState) {
                    is AuthState.Loading -> Text(
                        text = if (isRegistering) "Registrierung läuft..." else "Anmeldung läuft...",
                        color = MaterialTheme.colorScheme.primary
                    )

                    is AuthState.Success -> Text(
                        text = "Willkommen ${state.response.name}!",
                        color = MaterialTheme.colorScheme.primary
                    )

                    is AuthState.Error -> Text(
                        text = "Fehler: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )

                    AuthState.Idle -> Text(
                        text = "Bereit zur Anmeldung",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}