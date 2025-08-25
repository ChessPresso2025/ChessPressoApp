package app.chesspresso.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import app.chesspresso.ui.theme.Creme1

@Composable
fun InfoScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = authState) {
                is AuthState.Success -> {
                    Card(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Aktueller Benutzer:",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = state.response.name,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = state.response.email,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }

                    Card(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Serverstatus:",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = "Verbunden mit Server ✓",
                            fontSize = 16.sp,
                            color = Color.Green,
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }


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

                else -> {
                    Text(
                        text = "Laden...",
                        fontSize = 18.sp,
                        color = Creme1
                    )
                }
            }
        }
    }
}