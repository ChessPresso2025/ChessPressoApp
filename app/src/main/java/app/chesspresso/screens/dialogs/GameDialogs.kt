package app.chesspresso.screens.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ExperimentalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.model.GameDuration
import app.chesspresso.model.TeamColor

@Composable
private fun DialogContainer(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        )
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
        ) {
            Box {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Schließen")
                }
                content()
            }
        }
    }
}

@Composable
fun PrivateGameChoiceScreen(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(
                text = "Privates Spiel",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privates Spiel erstellen")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onJoinClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privatem Spiel beitreten")
            }
        }
    }
}

@Composable
fun JoinPrivateGameScreen(
    onDismiss: () -> Unit,
    onJoin: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text("Privatem Spiel beitreten", modifier = Modifier.padding(bottom = 16.dp))
                var lobbyId = remember { mutableStateOf("") }
                TextField(
                    value = lobbyId.value,
                    onValueChange = { if (it.length <= 6) lobbyId.value = it },
                    label = { Text("Lobby ID (6 Stellen)") },
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = onJoin,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = lobbyId.value.length == 6
                ) {
                    Text("Beitreten")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePublicGameScreen(
    onDismiss: () -> Unit,
    onCreateGame: (duration: GameDuration) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(GameDuration.MEDIUM) }
    var expanded by remember { mutableStateOf(false) }

    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Öffentliches Spiel erstellen",
                style = MaterialTheme.typography.headlineMedium
            )

            // Dropdown für Spieldauer (nur kurz/mittel/lang)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedDuration.description ?: selectedDuration.toString(),
                    onValueChange = { },
                    label = { Text("Spieldauer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    listOf(GameDuration.SHORT, GameDuration.MEDIUM, GameDuration.LONG).forEach { duration ->
                        DropdownMenuItem(
                            text = { Text(duration.description ?: duration.toString()) },
                            onClick = {
                                selectedDuration = duration
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onCreateGame(selectedDuration) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spiel erstellen")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePrivateGameScreen(
    onDismiss: () -> Unit,
    onCreateGame: (duration: GameDuration, color: TeamColor) -> Unit
) {
    var selectedDuration by remember { mutableStateOf(GameDuration.MEDIUM) }
    var selectedColor by remember { mutableStateOf(TeamColor.RANDOM) }
    var expanded by remember { mutableStateOf(false) }

    DialogContainer(onDismiss = onDismiss) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Privates Spiel erstellen",
                style = MaterialTheme.typography.headlineMedium
            )

            // Dropdown für Spieldauer
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    readOnly = true,
                    value = selectedDuration.description ?: selectedDuration.toString(),
                    onValueChange = { },
                    label = { Text("Spieldauer") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    GameDuration.entries.forEach { duration ->
                        DropdownMenuItem(
                            text = { Text(duration.description ?: duration.toString()) },
                            onClick = {
                                selectedDuration = duration
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Radio Buttons für Farbauswahl
            Column {
                Text(
                    "Spielerfarbe",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                TeamColor.entries.forEach { color ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (color == selectedColor),
                                onClick = { selectedColor = color }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (color == selectedColor),
                            onClick = { selectedColor = color }
                        )
                        Text(
                            text = color.description,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onCreateGame(selectedDuration, selectedColor) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spiel erstellen")
            }
        }
    }
}
