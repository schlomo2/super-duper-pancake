package com.instantiasoft.nqueens.data.model

data class NQueen(
    val x: Float = 0f,
    val y: Float = 0f,
    override val index: Int,
    override val square: Square? = null,
    override val moves: List<Square> = emptyList(),
    override val light: Boolean = false,
    override val moved: Boolean = false
): Piece(PieceType.NQueen, index = index, light = true) {
    override fun move(from: Square, to: Square, board: ChessBoard): MoveResult? {
        straight(from, to, board)?.let {
            return it
        }

        return diagonal(from, to, board)
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}