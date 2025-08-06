package app.chesspresso.auth.presemtation

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.navigation.NavController
import app.chesspresso.auth.presentation.AuthState
import app.chesspresso.auth.presentation.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()

    // Launcher für Google Sign-In ohne ID Token (funktioniert immer)
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("LoginScreen", "Google Sign-In result: ${result.resultCode}")

        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(Exception::class.java)

                if (account?.id != null && account.email != null) {
                    Log.d("LoginScreen", "Google Sign-In successful, sending to server...")
                    viewModel.loginWithGoogleAlternative(account.id!!, account.email!!)
                } else {
                    Log.e("LoginScreen", "Google account data incomplete")
                    viewModel.setErrorMessage("Google-Account-Daten unvollständig")
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google Sign-In error: ${e.message}", e)
                viewModel.setErrorMessage("Google Sign-In Fehler: ${e.message}")
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.w("LoginScreen", "Google Sign-In was cancelled by user")
            viewModel.setErrorMessage("Anmeldung wurde abgebrochen")
        } else {
            Log.w("LoginScreen", "Google Sign-In failed with result code: ${result.resultCode}")
            viewModel.setErrorMessage("Google Sign-In fehlgeschlagen")
        }
    }

    // UI
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Button(
                onClick = {
                    Log.d("LoginScreen", "Starting Google Sign-In...")

                    try {

                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .requestProfile()
                            .requestId()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)

                        googleSignInClient.signOut().addOnCompleteListener {
                            Log.d("LoginScreen", "Previous sign-out completed, launching sign-in")
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)
                        }
                        navController.navigate("home_screen")
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Error starting Google Sign-In: ${e.message}", e)
                        viewModel.setErrorMessage("Fehler beim Starten der Google-Anmeldung: ${e.message}")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = authState !is AuthState.Loading
            ) {
                Text("Mit Google anmelden")
            }



            when (val state = authState) {
                is AuthState.Loading -> Text("Anmeldung läuft...")
                is AuthState.Success -> Text("Willkommen ${state.response.name}!")
                is AuthState.Error -> Text("Fehler: ${state.message}")
                AuthState.Idle -> Text("Bereit zum Anmelden")
            }
        }
    }
}