package com.instantiasoft.nqueens.model

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
    val piece: Piece,
    val direction: MoveDirection
)
