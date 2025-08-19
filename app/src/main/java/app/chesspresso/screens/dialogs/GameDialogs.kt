package app.chesspresso.screens.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.chesspresso.model.lobby.GameDuration
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
            modifier = Modifier.fillMaxSize()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameDurationDropdown(
    selectedDuration: GameDuration,
    onDurationSelected: (GameDuration) -> Unit,
    availableDurations: List<GameDuration> = GameDuration.entries
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedDuration.displayName,
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
            availableDurations.forEach { duration ->
                DropdownMenuItem(
                    text = { Text(duration.displayName) },
                    onClick = {
                        onDurationSelected(duration)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun DialogContent(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        content()
    }
}

@Composable
fun PrivateGameChoiceScreen(
    onCreateClick: () -> Unit,
    onJoinClick: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogContainer(onDismiss = onDismiss) {
        DialogContent(title = "Privates Spiel") {
            Button(onClick = onCreateClick, modifier = Modifier.fillMaxWidth()) {
                Text("Privates Spiel erstellen")
            }
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
        DialogContent(title = "Privatem Spiel beitreten") {
            var lobbyId by remember { mutableStateOf("") }
            TextField(
                value = lobbyId,
                onValueChange = { if (it.length <= 6) lobbyId = it },
                label = { Text("Lobby ID (6 Stellen)") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                enabled = lobbyId.length == 6
            ) {
                Text("Beitreten")
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
    val availableDurations = listOf(GameDuration.SHORT, GameDuration.MEDIUM, GameDuration.LONG)

    DialogContainer(onDismiss = onDismiss) {
        DialogContent(title = "Öffentliches Spiel erstellen") {
            GameDurationDropdown(
                selectedDuration = selectedDuration,
                onDurationSelected = { selectedDuration = it },
                availableDurations = availableDurations
            )
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

    DialogContainer(onDismiss = onDismiss) {
        DialogContent(title = "Privates Spiel erstellen") {
            GameDurationDropdown(
                selectedDuration = selectedDuration,
                onDurationSelected = { selectedDuration = it }
            )

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

            Button(
                onClick = { onCreateGame(selectedDuration, selectedColor) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Spiel erstellen")
            }
        }
    }
}
