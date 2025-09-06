package app.chesspresso.screens.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import app.chesspresso.data.storage.ThemeStorage
import app.chesspresso.ui.theme.CoffeeText
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import app.chesspresso.ui.theme.CoffeeCard
import app.chesspresso.ui.theme.CoffeeHeadlineText

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val darkThemeFlow = ThemeStorage.getDarkThemeFlow(context)
    val darkTheme by darkThemeFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter // Änderung: alles oben ausrichten
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()// Optional: Abstand zum oberen Rand
        ) {
            CoffeeHeadlineText("Einstellungen")
            // Abstand
            Spacer(modifier = Modifier.padding(16.dp))
            // Theme-Switch
            CoffeeCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoffeeText(text = if (darkTheme) "Dunkles Theme aktiviert" else "Helles Theme aktiviert")
                    Spacer(modifier = Modifier.weight(1f)) // Schiebt den Switch ganz nach rechts
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = { checked ->
                            scope.launch {
                                ThemeStorage.setDarkTheme(context, checked)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurface,
                            uncheckedTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        )
                    )
                }

            }

        }
    }
}