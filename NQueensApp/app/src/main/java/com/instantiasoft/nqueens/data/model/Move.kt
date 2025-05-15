package com.instantiasoft.nqueens.data.model

enum class MoveDirection {
    North,
    NorthEast,
    East,
    SouthEast,
    South,
    SouthWest,
    West,
    NorthWest
}

data class Move(
    val pieceIndex: Int?,
    val direction: MoveDirection,
    val collision: Boolean = false
)
