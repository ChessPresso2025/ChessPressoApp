package app.chesspresso.model.lobby

enum class LobbyType {
    PRIVATE,
    PUBLIC
}

enum class LobbyStatus {
    WAITING,
    FULL,
    IN_GAME,
    CLOSED
}

data class Lobby(
    val lobbyId: String,
    val lobbyType: LobbyType,
    val gameTime: GameTime?,
    val players: List<String>,
    val creator: String,
    val isGameStarted: Boolean,
    val status: LobbyStatus,
    val whitePlayer: String? = null,
    val blackPlayer: String? = null,
    val randomColors: Boolean = false
) {
    fun isFull(): Boolean = players.size >= 2
    fun isPublic(): Boolean = lobbyType == LobbyType.PUBLIC
    fun isPrivate(): Boolean = lobbyType == LobbyType.PRIVATE
}
