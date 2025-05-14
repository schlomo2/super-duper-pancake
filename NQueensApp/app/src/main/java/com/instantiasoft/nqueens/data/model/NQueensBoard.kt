package com.instantiasoft.nqueens.data.model

data class NQueensBoard(
    val squares: Array<Array<Square>> = emptyArray(),
) {
    val size: Int get() = squares.size

    fun square(row: Int, col: Int): Square? {
        return squares.getOrNull(row)?.getOrNull(col)
    }

    fun updateSquare(square: Square): NQueensBoard {
        square.row.takeIf { it in 0..<size }?.let { row ->
            square.col.takeIf { it in 0..<size }?.let { col ->
                val updatedSquares = squares.copyOf()
                updatedSquares[row][col] = square
                return NQueensBoard(updatedSquares)
            }
        }

        return this
    }

    fun updateSquare(row: Int, col: Int, piece: NQueen?): NQueensBoard {
        row.takeIf { it in 0..<size }?.let {
            col.takeIf { it in 0..<size }?.let {
                val updatedSquares = squares.copyOf()
                updatedSquares[row][col] = updatedSquares[row][col].copy(pieceIndex = piece?.index)
                return NQueensBoard(updatedSquares)
            }
        }

        return this
    }

    fun updateSquares(squareList: List<Square>): NQueensBoard {
        val updatedSquares = squares.copyOf()
        squareList.forEach { square ->
            square.row.takeIf { it in 0..<size }?.let { row ->
                square.col.takeIf { it in 0..<size }?.let { col ->
                    updatedSquares[row][col] = square
                }
            }
        }

        return NQueensBoard(updatedSquares)
    }

    fun overSquare(dragPosX: Float, dragPosY: Float): Square? {
        squares.forEach { row ->
            row.forEach { square ->
                if (square.position.x < dragPosX && square.position.y < dragPosY) {
                    if (dragPosX < square.position.x + square.size.width && dragPosY < square.position.y + square.size.height) {
                        return square
                    }
                }
            }
        }

        return null
    }

    fun hasPiece(row: Int, col: Int): Boolean {
        return squares.getOrNull(row)?.getOrNull(col)?.pieceIndex != null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NQueensBoard

        return squares.contentDeepEquals(other.squares)
    }

    override fun hashCode(): Int {
        return squares.contentDeepHashCode()
    }
}
