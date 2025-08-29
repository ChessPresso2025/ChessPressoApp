package app.chesspresso.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.statsState.collectAsState()
    // Stats beim ersten Anzeigen laden
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadStats()
    }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (uiState) {
            is StatsUiState.Loading -> Text("Lade Statistiken...")
            is StatsUiState.Error -> Text("Fehler: " + (uiState as StatsUiState.Error).message)
            is StatsUiState.Success -> {
                val stats = (uiState as StatsUiState.Success).stats
                Text("Siege: ${stats.wins}\nNiederlagen: ${stats.losses}\nRemis: ${stats.draws}\nGesamt: ${stats.wins + stats.losses + stats.draws}")
            }
        }
    }
}
