package app.chesspresso.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object QuickMatch : Screen("quick_match")
    object PrivateLobby : Screen("private_lobby")
    object LobbyWaiting : Screen("lobby_waiting/{lobbyCode}") {
        fun createRoute(lobbyCode: String) = "lobby_waiting/$lobbyCode"
    }

    object Game : Screen("game/{lobbyId}") {
        fun createRoute(lobbyId: String) = "game/$lobbyId"
    }
}
