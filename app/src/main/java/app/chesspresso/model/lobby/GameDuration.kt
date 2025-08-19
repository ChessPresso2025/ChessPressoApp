package app.chesspresso.model.lobby

enum class GameDuration(val seconds: Int, val displayName: String) {
    SHORT(300, "Kurz (5 Min)"),
    MEDIUM(900, "Mittel (15 Min)"),
    LONG(1800, "Lang (30 Min)"),
    UNLIMITED(-1, "Unbegrenzt");

    fun isUnlimited(): Boolean = this == UNLIMITED
}
