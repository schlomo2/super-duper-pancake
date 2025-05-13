package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.instantiasoft.nqueens.R
import com.instantiasoft.nqueens.data.model.Move
import com.instantiasoft.nqueens.data.model.MoveDirection
import com.instantiasoft.nqueens.data.model.Square


@Composable
fun MoveIcon(
    square: Square,
    move: Move,
    multiple: Boolean
) {
    val density = LocalDensity.current
    val imageRotation = when(move.direction) {
        MoveDirection.North -> -90f
        MoveDirection.NorthEast -> -45f
        MoveDirection.East -> 0f
        MoveDirection.SouthEast -> 45f
        MoveDirection.South -> 90f
        MoveDirection.SouthWest -> 135f
        MoveDirection.West -> 180f
        MoveDirection.NorthWest -> -135f
    }

    if (move.collision) {
        Icon(
            painter = painterResource(if (multiple) R.drawable.circle else R.drawable.arrow_right),
            contentDescription = move.direction.name,
            modifier = Modifier.size(if (multiple) 12.dp else 24.dp).rotate(imageRotation),
            tint = Color.Red
        )
    }

    Icon(
        painter = painterResource(R.drawable.arrow_right),
        contentDescription = move.direction.name,
        modifier = Modifier.rotate(imageRotation)
            .offset(x = with(density) { (square.size.width * -0.33f).toDp() }),
        tint = if (move.collision) Color.Red else Color.Black
    )
}