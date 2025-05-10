package com.instantiasoft.nqueens.ui.fireworks

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instantiasoft.nqueens.R
import com.instantiasoft.nqueens.model.Projectile
import com.instantiasoft.nqueens.ui.nqueens.NQueensViewModel
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt


@Composable
fun Rocket(
    projectile: Projectile
) {
    Icon(
        painter = painterResource(R.drawable.rocket),
        contentDescription = null,
        modifier = Modifier
            .size(16.dp).offset {
                IntOffset(projectile.offset.x.roundToInt(),
                    projectile.offset.y.roundToInt())
            }
            .rotate(90f + atan2(-projectile.velocityY, projectile.velocityX) * 180f / PI.toFloat())
    )
}