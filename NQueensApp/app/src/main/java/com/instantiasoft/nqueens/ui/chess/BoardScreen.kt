package com.instantiasoft.nqueens.ui.chess

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BoardScreen(
    size: Int,
    boardViewModel: BoardViewModel = hiltViewModel<BoardViewModel, BoardViewModel.BoardViewModelFactory> { factory ->
        factory.create(size)
    }
) {
    val config = LocalConfiguration.current
    val screenWidthDp = config.screenWidthDp.dp
    val screenHeightDp = config.screenHeightDp.dp
    val padding = 0.dp

    val minDimension = if (screenWidthDp <= screenHeightDp) screenWidthDp else screenHeightDp

    val minBoardSize = minDimension - padding * 2
    val squareSize = minBoardSize / 8

    val boardState by boardViewModel.boardState.collectAsStateWithLifecycle()

    val lightColor = Color(0xfffefecc)
    val darkColor = Color(0xff078900)

    var lightSquare = true

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                boardState.lightCaptured.mapNotNull { it.type.name.firstOrNull() }
                    .joinToString(","),
                color = Color.White
            )

            Column(
                modifier = Modifier.padding(padding)
            ) {
                for (rowIndex in boardState.rows.lastIndex.downTo(0)) {
                    Row {
                        boardState.rows[rowIndex].forEachIndexed { colIndex, col ->
                            Box(
                                modifier = Modifier
                                    .size(squareSize)
                                    .background(
                                        when {
                                            col.active -> Color.Yellow
                                            lightSquare -> lightColor
                                            else -> darkColor
                                        }
                                    ).clickable {
                                        boardState.onClick(rowIndex, colIndex)
                                    }
                            ) {
                                col.piece?.let { piece ->
                                    Box(
                                        modifier = Modifier.align(Alignment.Center)
                                    ) {
                                        if (piece.light) {
                                            Text(
                                                text = piece.type.getChar(),
                                                fontSize = 40.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                modifier = Modifier.offset(2.dp, 2.dp)
                                            )
                                        }
                                        Text(
                                            text = piece.type.getChar(),
                                            fontSize = 40.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (piece.light) Color.White else Color.Black,
                                        )
                                    }
                                }
                            }

                            lightSquare = !lightSquare
                        }

                        lightSquare = !lightSquare
                    }
                }
            }

            Text(
                boardState.darkCaptured.mapNotNull { it.type.name.firstOrNull() }.joinToString(","),
                color = Color.White
            )
        }


    }
}
