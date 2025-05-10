package com.instantiasoft.nqueens.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class Square(
    val row: Int,
    val col: Int,
    val light: Boolean,
    val piece: Piece? = null,
    val moves: List<Move> = emptyList(),
    val active: Boolean = false,
    val position: Offset = Offset(0f,0f),
    val size: IntSize = IntSize(0, 0)
) {
    companion object {
        fun isLight(row: Int, col: Int, startsWithLight: Boolean = true): Boolean {
            var light = startsWithLight
            var initRow = 0
            while (initRow < row) {
                initRow++
                light = !light
            }

            var initCol = 0
            while(initCol < col) {
                initCol++
                light = !light
            }

            return light
        }
    }
}
