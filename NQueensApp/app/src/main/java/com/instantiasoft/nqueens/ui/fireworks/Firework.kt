package com.instantiasoft.nqueens.ui.fireworks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instantiasoft.nqueens.data.model.Projectile
import com.instantiasoft.nqueens.ui.nqueens.NQueensViewModel
import kotlin.math.roundToInt

@Composable
fun Firework(projectile: Projectile) {
    val fadingPercent = if (projectile.elapsed < projectile.duration) {
        1f
    } else {
        1 - ((projectile.elapsed - projectile.duration).toFloat()/projectile.fadeDuration)
    }

    val growingPercent = if (projectile.elapsed < 300) {
        (projectile.elapsed.toFloat() + 1) / 300
    } else 1f

    Box(modifier = Modifier
        .size(16.dp * growingPercent * fadingPercent)
        .offset {
            IntOffset(projectile.offset.x.roundToInt(),
                projectile.offset.y.roundToInt())
        }
        .clip(CircleShape)
        .background(
            projectile.color.color.copy(
                alpha = fadingPercent
            )
        )
    )
}