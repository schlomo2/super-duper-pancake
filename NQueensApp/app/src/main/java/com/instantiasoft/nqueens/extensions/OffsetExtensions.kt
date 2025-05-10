package com.instantiasoft.nqueens.extensions

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt

val Offset.magnitude: Float get() = sqrt(x * x + y * y)
fun Offset.distance(other: Offset): Float {
    return (other - this).magnitude
}
