package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.instantiasoft.nqueens.R
import com.instantiasoft.nqueens.data.model.Collision
import com.instantiasoft.nqueens.data.model.NQueen
import kotlin.math.roundToInt

@Composable
fun QueenToken(
    queensState: NQueensViewModel.BoardState,
    nQueen: NQueen,
    index: Int
) {
    Box(
        modifier = Modifier
            .offset {
                IntOffset(nQueen.x.roundToInt(), nQueen.y.roundToInt())
            }.padding(2.dp)
            .clip(
                CircleShape
            ).size(queensState.squareSizeDp.dp)
            .background(
                color = nQueen.square?.let { square ->
                    Color.Red.takeIf { (queensState.collisionMap[Collision(square.row, square.col)]?.count() ?: 0) > 0 } ?:
                    Color(0xff00aa55)
                } ?: Color(0xff96836b), CircleShape
            )
            .padding(1.5.dp)
            .border(2.dp, Color.White, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        queensState.onDragEnd(index)
                    }
                ) { change, dragAmount ->
                    change.consume()
                    queensState.onDrag(index, dragAmount)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.crown),
            contentDescription = "Queen",
            modifier = Modifier.padding(4.dp).fillMaxSize(),
            tint = Color.White
        )
    }
}
