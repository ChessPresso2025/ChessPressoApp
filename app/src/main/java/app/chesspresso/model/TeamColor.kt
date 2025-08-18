package app.chesspresso.model

import kotlinx.coroutines.internal.OpDescriptor

enum class TeamColor(val description: String) {
    BLACK("Schwarz"),
    WHITE("Weiß"),
    RANDOM("Zufällig")
}