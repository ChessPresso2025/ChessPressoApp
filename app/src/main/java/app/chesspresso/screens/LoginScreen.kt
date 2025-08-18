package app.chesspresso.auth.presemtation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavController
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()
    var isRegistering by remember { mutableStateOf(false) }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    // Vorausgefüllter Benutzername, falls vorhanden
    val storedUsername = remember { viewModel.getStoredUsername() }
    if (username.isEmpty() && storedUsername != null) {
        username = storedUsername
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

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Benutzername") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = authState !is AuthState.Loading
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Passwort") },
                    visualTransformation = PasswordVisualTransformation(),
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (isRegistering) {
                            if (username.isNotBlank() && password.isNotBlank() && email.isNotBlank()) {
                                viewModel.register(username.trim(), password, email.trim())
                            } else {
                                viewModel.setErrorMessage("Alle Felder müssen ausgefüllt werden")
                            }
                        } else {
                            if (username.isNotBlank() && password.isNotBlank()) {
                                viewModel.login(username.trim(), password)
                            } else {
                                viewModel.setErrorMessage("Benutzername und Passwort müssen ausgefüllt werden")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthState.Loading
                ) {
                    Text(if (isRegistering) "Registrieren" else "Anmelden")
                }

                Row {
                    Text(
                        text = if (isRegistering) "Bereits ein Konto?" else "Noch kein Konto?",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = {
                            isRegistering = !isRegistering
                            email = "" // Reset email field when switching
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
    }
}