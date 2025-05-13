package com.instantiasoft.nqueens.data.model

import kotlin.math.abs

enum class PieceType {
    Pawn,
    Knight,
    Bishop,
    Rook,
    Queen,
    King,
    NQueen;

    fun getChar(): String {
        if (this == Knight) return "k"
        return name.first().toString()
    }
}

abstract class Piece(
    val type: PieceType,
    open val index: Int = 0,
    open val square: Square? = null,
    open val moves: List<Square> = emptyList(),
    open val light: Boolean,
    open val moved: Boolean = false
) {
    abstract fun moved(): Piece
    abstract fun move(from: Square, to: Square, board: ChessBoard): MoveResult?

    companion object {
        fun testPath(from: Square, to: Square, rowOffset: Int, colOffset: Int, board: ChessBoard): MoveResult? {
            var row = from.row + rowOffset
            var col = from.col + colOffset

            while(row >= 0 && row < board.size && col >= 0 && col < board.size) {
                if (row == to.row && col == to.col) {
                    if (to.piece == null) {
                        return MoveResult(MoveType.Moved)
                    }

                    if (to.piece.light != from.piece?.light) {
                        return MoveResult(MoveType.Capture, to)
                    }

                    return null
                }

                board.getSquare(row, col)?.takeIf { it.piece != null }?.let {
                    return null
                }

                row += rowOffset
                col += colOffset
            }

            return null
        }

        fun diagonal(from: Square, to: Square, board: ChessBoard): MoveResult? {
            if (from.light != to.light) return null
            if (from.row == to.row) return null
            if (from.col == to.col) return null

            val rowDiff = to.row - from.row
            val colDiff = to.col - from.col

            if (abs(colDiff) != abs(rowDiff)) return null

            if (rowDiff < 0) {
                if (colDiff < 0) {
                    return testPath(from = from, to = to, -1, -1, board)
                } else {
                    return testPath(from = from, to = to, -1, 1, board)
                }
            } else {
                if (colDiff < 0) {
                    return testPath(from = from, to = to, 1, -1, board)
                } else {
                    return testPath(from = from, to = to, 1, 1, board)
                }
            }
        }

        fun straight(from: Square, to: Square, board: ChessBoard): MoveResult? {
            val rowDiff = to.row - from.row
            val colDiff = to.col - from.col

            if ((rowDiff == 0 && colDiff == 0) ||
                (rowDiff != 0 && colDiff != 0)) {
                return null
            }

            if (rowDiff == 0) {
                if (colDiff < 0) {
                    return testPath(from, to, 0, -1, board)
                } else {
                    return testPath(from, to, 0, 1, board)
                }
            } else {
                if (rowDiff < 0) {
                    return testPath(from, to, -1, 0, board)
                } else {
                    return testPath(from, to, 1, 0, board)
                }
            }
        }
    }
}

data class Pawn(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.Pawn, light = light, moved = moved) {
    override fun move(from: Square, to: Square, board: ChessBoard): MoveResult? {
        val colDiff = to.col - from.col
        val rowDiff = to.row - from.row

        // move
        if (colDiff == 0) {
            if (light) {
                if (rowDiff == 1) {
                    if (to.piece == null) {
                        return MoveResult.moved
                    }

                    return null
                }

                if (rowDiff == 2 && !moved) {
                    if (!board.isSquareEmpty(from.row + 1, from.col)) {
                        return null
                    }

                    if (to.piece != null) {
                        return null
                    }

                    return MoveResult.moved
                }

                return null
            }

            // dark
            if (rowDiff == -1) {
                if (to.piece == null) {
                    return MoveResult.moved
                }

                return null
            }

            if (rowDiff == -2 && !moved) {
                if (!board.isSquareEmpty(to.row + 1, from.col)) {
                    return null
                }

                if (to.piece != null) {
                    return null
                }

                return MoveResult.moved
            }

            return null
        }

        // capture
        if (abs(colDiff) == 1) {
            if (light) {
                if (rowDiff == 1) {
                    to.piece?.let {
                        if (!it.light) {
                            return MoveResult(MoveType.Capture, to)
                        }
                    } ?: board.enPassant?.let {
                        if (it.col == to.col && it.row == from.row) {
                            return MoveResult(MoveType.EnPassant, it)
                        }
                    }
                }
            } else if (rowDiff == -1) {
                to.piece?.let {
                    if (it.light) {
                        return MoveResult(MoveType.Capture, to)
                    }
                } ?: board.enPassant?.let {
                    if (it.col == to.col && it.row == from.row) {
                        return MoveResult(MoveType.EnPassant, it)
                    }
                }
            }
        }

        return null
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}

data class Knight(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.Knight, light = light, moved = moved) {
    override fun move(from: Square, to: Square, board: ChessBoard): MoveResult? {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col

        if ((abs(rowDiff) == 1 && abs(colDiff) == 2) ||
            (abs(rowDiff) == 2 && abs(colDiff) == 1)) {
            if (to.piece == null) {
                return MoveResult(MoveType.Moved)
            }

            if (to.piece.light == from.piece?.light) {
                return null
            }

            return MoveResult(MoveType.Capture, to)
        }

        return null
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}

data class Bishop(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.Bishop, light = light, moved = moved) {
    override fun move(from: Square, to: Square, board: ChessBoard): MoveResult? {
        return diagonal(from, to, board)
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}

data class Rook(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.Rook, light = light, moved = moved) {
    override fun move(from: Square, to: Square, board: ChessBoard): MoveResult? {
        return straight(from, to, board)
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}

data class Queen(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.Queen, light = light, moved = moved) {
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

data class King(
    override val light: Boolean, override val moved: Boolean = false
): Piece(type = PieceType.King, light = light, moved = moved) {
    override fun move(from: Square, to: Square, chessBoard: ChessBoard): MoveResult? {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col

        if (!moved && rowDiff == 0 && abs(colDiff) > 1) {
            if (colDiff < 0) {
                var col = from.col - 1
                while (col >= 0) {
                    val square = chessBoard.getSquare(from.row, col) ?: return null
                    square.piece?.let {
                        if (it.type != PieceType.Rook || it.moved) return null
                        return MoveResult(MoveType.Castle)
                    }

                    col--
                }
            } else {
                var col = from.col + 1
                while (col < chessBoard.size) {
                    val square = chessBoard.getSquare(from.row, col) ?: return null
                    square.piece?.let {
                        if (it.type != PieceType.Rook || it.moved) return null
                        return MoveResult(MoveType.Castle)
                    }

                    col++
                }
            }
        }

        if (abs(rowDiff) > 1 || abs(colDiff) > 1) {
            return null
        }

        straight(from, to, chessBoard)?.let {
            return it
        }

        return diagonal(from, to, chessBoard)
    }

    override fun moved(): Piece {
        return copy(moved = true)
    }
}


