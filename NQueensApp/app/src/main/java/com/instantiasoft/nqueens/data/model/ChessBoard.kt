package com.instantiasoft.nqueens.data.model

data class ChessBoard(
    val size: Int = 8,
    val rows: List<List<Square>> = listOf(),
    val activeRow: Int? = null,
    val activeSquare: Square? = null,
    val onClick: (row: Int, col: Int) -> Unit,
    val enPassant: Square? = null,
    val lightTurn: Boolean = true,
    val lightCaptured: List<Piece> = emptyList(),
    val darkCaptured: List<Piece> = emptyList()
) {
    fun getSquare(row: Int, col: Int): Square? {
        return rows.getOrNull(row)?.getOrNull(col)
    }

    fun isSquareEmpty(row: Int, col: Int): Boolean {
        getSquare(row, col)?.let {
            return it.piece == null
        }

        return false
    }
}