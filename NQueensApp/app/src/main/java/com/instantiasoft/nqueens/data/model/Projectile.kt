package com.instantiasoft.nqueens.data.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ProjectileType {
    Rocket,
    Firework
}

enum class ProjectileColor(val color: Color) {
    Red(Color(0xffff3322)),
    Orange(Color(0xffff8800)),
    Yellow(Color.Yellow),
    Green(Color.Green),
    Blue(Color.Blue),
    Purple(Color(0xff9900ff)),
    Silver(Color(0xff998877)),
    White(Color(0xfff1f1f1));

    companion object {
        fun get(random: Double): ProjectileColor {
            return entries.let { enums ->
                enums.find { it.ordinal == (random * enums.size).toInt() } ?: White
            }
        }
    }
}

data class Projectile(
    val type: ProjectileType,
    val color: ProjectileColor,
    val duration: Long,
    val velocityX: Float,
    val velocityY: Float,
    val velocityZ: Float = 0f,
    val fadeDuration: Long = 0,
    val offset: Offset = Offset.Zero,
    val elapsed: Long = 0,
    val millis: Long = System.currentTimeMillis(),
)