package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
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
fun DoneToken(
    boardState: NQueensViewModel.BoardState
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .clip(
                CircleShape
            ).size(boardState.squareSizeDp.dp)
            .background(
                color = Color(0xff00aa55)
            )
            .padding(1.5.dp)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = "Done",
            modifier = Modifier.padding(4.dp).fillMaxSize(),
            tint = Color.White
        )
    }
}
