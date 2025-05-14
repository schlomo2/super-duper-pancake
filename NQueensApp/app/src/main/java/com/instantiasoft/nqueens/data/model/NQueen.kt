package com.instantiasoft.nqueens.data.model

data class NQueen(
    val x: Float = 0f,
    val y: Float = 0f,
    val index: Int,
    val square: Square? = null,
    val moves: List<Square> = emptyList(),
    val light: Boolean = false,
    val moved: Boolean = false
)