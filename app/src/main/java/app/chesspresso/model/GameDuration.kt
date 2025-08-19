package app.chesspresso.model

enum class GameDuration( val minutes: Int, val description: String? = null) {
    SHORT(5, "kurzes Spiel"), // 5 minutes
    MEDIUM(15, "mittleres Spiel"), // 15 minutes
    LONG(30, "langes Spiel"), // 30 minutes
    UNLIMITED(0, "unbegrenzt") // no time limit, for private games
}