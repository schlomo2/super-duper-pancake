package com.instantiasoft.nqueens.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class Square(
    val row: Int,
    val col: Int,
    val light: Boolean,
    val pieceIndex: Int? = null,
    val active: Boolean = false,
    val position: Offset = Offset(0f,0f),
    val size: IntSize = IntSize(10, 10)
) {
    fun sameBoardPosition(square: Square?): Boolean {
        return row == square?.row && col == square.col
    }

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
