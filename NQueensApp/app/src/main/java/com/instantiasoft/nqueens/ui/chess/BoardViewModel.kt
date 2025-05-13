package com.instantiasoft.nqueens.ui.chess

import androidx.lifecycle.ViewModel
import com.instantiasoft.nqueens.data.model.Bishop
import com.instantiasoft.nqueens.data.model.ChessBoard
import com.instantiasoft.nqueens.data.model.King
import com.instantiasoft.nqueens.data.model.Knight
import com.instantiasoft.nqueens.data.model.MoveType
import com.instantiasoft.nqueens.data.model.Pawn
import com.instantiasoft.nqueens.data.model.PieceType
import com.instantiasoft.nqueens.data.model.Queen
import com.instantiasoft.nqueens.data.model.Rook
import com.instantiasoft.nqueens.data.model.Square
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

@HiltViewModel(assistedFactory = BoardViewModel.BoardViewModelFactory::class)
class BoardViewModel @AssistedInject constructor(
    @Assisted val size: Int
) : ViewModel() {
    @AssistedFactory
    interface BoardViewModelFactory {
        fun create(size: Int): BoardViewModel
    }

    private val _Chess_boardState = MutableStateFlow(
        ChessBoard(
            onClick = ::onClick
        )
    )
    val boardState = _Chess_boardState.asStateFlow()

    init {
        setupBoard()
    }

    fun setupBoard() {
        var light = false

        val rows = mutableListOf<List<Square>>().let { rows ->
            repeat(size) { row ->
                rows.add(
                    mutableListOf<Square>().let { cols ->
                        repeat(size) {
                            cols.add(Square(row = row, col = cols.count(), light = light))
                            light = !light
                        }
                        light = !light
                        cols
                    }
                )
            }

            rows
        }

        _Chess_boardState.update {
            it.copy(
                size = size,
                rows = rows
            )
        }

        addPawnsToRow(1, true)
        addPawnsToRow(6, false)

        addBasePiecesToRow(0, true)
        addBasePiecesToRow(7, false)
    }

    fun addPawnsToRow(rowIndex: Int, light: Boolean) {
        val size = _Chess_boardState.value.size
        val row = _Chess_boardState.value.rows.getOrNull(rowIndex)?.toMutableList() ?: return

        repeat(size) {
            row[it] = row[it].copy(
                piece = Pawn(
                    light = light
                )
            )
        }

        _Chess_boardState.update {
            it.copy(
                rows = it.rows.toMutableList().apply {
                    this[rowIndex] = row
                }
            )
        }
    }

    fun addBasePiecesToRow(rowIndex: Int, light: Boolean) {
        val size = _Chess_boardState.value.size
        val row = _Chess_boardState.value.rows.getOrNull(rowIndex)?.toMutableList() ?: return

        row[0] = row[0].copy(
            piece = Rook(
                light = light
            ),
        )

        row[size-1] = row[size-1].copy(
            piece = Rook(
                light = light
            )
        )

        row[1] = row[1].copy(
            piece = Knight(
                light = light
            )
        )

        row[size-2] = row[size-2].copy(
            piece = Knight(
                light = light
            )
        )

        row[2] = row[2].copy(
            piece = Bishop(
                light = light
            )
        )

        row[size-3] = row[size-3].copy(
            piece = Bishop(
                light = light
            )
        )

        row[3] = row[3].copy(
            piece = Queen(
                light = light
            )
        )

        row[size-4] = row[size-4].copy(
            piece = King(
                light = light
            )
        )

        _Chess_boardState.update {
            it.copy(
                rows = it.rows.toMutableList().apply {
                    this[rowIndex] = row
                }
            )
        }
    }

    fun onClick(row: Int, col: Int) {
        val clickedSquare = boardState.value.getSquare(row, col) ?: return
        val activeSquare = boardState.value.activeSquare

        activeSquare?.takeIf {
            it.piece?.light == boardState.value.lightTurn
        }?.let { fromSquare ->
            fromSquare.piece?.move(fromSquare, clickedSquare, boardState.value)?.let { moveResult ->
                val toSquare = clickedSquare.copy(piece = fromSquare.piece.moved(), active = false)

                _Chess_boardState.update { state ->
                    state.copy(
                        rows = state.rows.toMutableList().apply {
                            this[row] = this[row].toMutableList().apply {
                                this[col] = toSquare
                            }

                            this[fromSquare.row] = this[fromSquare.row].toMutableList().apply {
                                this[fromSquare.col] = fromSquare.copy(piece = null, active = false)
                            }

                            moveResult.square?.takeIf { moveResult.type == MoveType.EnPassant }?.let { square ->
                                this[square.row] = this[square.row].toMutableList().apply {
                                    this[square.col] = square.copy(piece = null, active = false)
                                }
                            }
                        },
                        activeSquare = null,
                        enPassant = toSquare.takeIf { toSquare.piece?.type == PieceType.Pawn && abs(row - fromSquare.row) == 2 },
                        lightCaptured = moveResult.square?.piece?.takeIf {
                            moveResult.isLightCapture
                        }?.let { capturedPiece ->
                            state.lightCaptured.plus(capturedPiece)
                        } ?: state.lightCaptured,
                        darkCaptured = moveResult.square?.piece?.takeIf {
                            moveResult.isDarkCapture
                        }?.let { capturedPiece ->
                            state.darkCaptured.plus(capturedPiece)
                        } ?: state.darkCaptured,
                        lightTurn = !state.lightTurn
                    )
                }

                return
            }
        }

        _Chess_boardState.update { state ->
            state.copy(
                rows = state.rows.toMutableList().apply {
                    this[row] = this[row].toMutableList().apply {
                        val square = this[col]
                        this[col] = square.copy(active = clickedSquare.piece?.let { !square.active } ?: false)
                    }

                    activeSquare?.let { (activeRow, activeCol) ->
                        if (activeRow != row || activeCol != col) {
                            this[activeRow] = this[activeRow].toMutableList().apply {
                                val square = this[activeCol]
                                this[activeCol] = square.copy(active = false)
                            }
                        }
                    }
                },
                activeSquare = activeSquare?.let {
                    if ((it.row != row || it.col != col) && clickedSquare.piece != null) {
                        clickedSquare
                    } else null
                } ?: clickedSquare.piece?.let { clickedSquare }
            )
        }
    }
}
