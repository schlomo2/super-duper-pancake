package com.instantiasoft.nqueens.model

enum class MoveType {
    Moved,
    Capture,
    EnPassant,
    Castle
}

data class MoveResult(val type: MoveType, val square: Square? = null) {
    val isLightCapture: Boolean get() = (type == MoveType.Capture || type == MoveType.EnPassant) && square?.piece?.light == true
    val isDarkCapture: Boolean get() = (type == MoveType.Capture || type == MoveType.EnPassant) && square?.piece?.light == false

    companion object {
        val moved = MoveResult(MoveType.Moved)
    }
}
