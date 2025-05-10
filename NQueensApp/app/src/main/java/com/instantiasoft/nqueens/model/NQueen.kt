package com.instantiasoft.nqueens.model

data class NQueen(
    val x: Float = 0f,
    val y: Float = 0f,
    override val light: Boolean = false,
    override val moved: Boolean = false
): Piece(PieceType.NQueen, true) {
    override fun move(from: Square, to: Square, board: Board): MoveResult? {
        straight(from, to, board)?.let {
            return it
        }

        return diagonal(from, to, board)
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}