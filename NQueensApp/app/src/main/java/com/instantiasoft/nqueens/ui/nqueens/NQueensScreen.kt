package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instantiasoft.nqueens.data.model.ProjectileType
import com.instantiasoft.nqueens.ui.fireworks.Firework
import com.instantiasoft.nqueens.ui.fireworks.Rocket
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NQueensScreen(
    size: Int,
    queensViewModel: NQueensViewModel = hiltViewModel<NQueensViewModel, NQueensViewModel.NQueensViewModelFactory> { factory ->
        factory.create(size)
    }
) {
    val config = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()

    val boardState by queensViewModel.boardState.collectAsStateWithLifecycle()
    val padding = 16
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(boardState) {
        if (boardState.needsSetup) {
            queensViewModel.setup(config.screenWidthDp-padding*2, config.screenHeightDp-padding*2)
        }
    }

    NQueensScreen(
        boardState = boardState,
        onSettings = {
            coroutineScope.launch {
                settingsSheetState.show()
            }
        }
    )

    if (settingsSheetState.isVisible) {
        SettingsSheet(
            boardState = boardState,
            sheetState = settingsSheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    settingsSheetState.hide()
                }
            }
        )
    }

    SideEffect {
        queensViewModel.updateAnimations()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NQueensScreen(
    boardState: NQueensViewModel.BoardState,
    onSettings: () -> Unit
) {

    val overColor = Color(0xff00ffff)
    val lightColor = Color(0xfffefecc)
    val darkColor = Color(0xff078900)

    val gradientColor1 = Color(0xffC7B299)
    val gradientColor2 = Color(0xffF5F0E4)

    val brush = Brush.linearGradient(
        colors = listOf(
            gradientColor2, gradientColor1
        ),
        start = Offset(0f, 0f),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "N-Queens",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = onSettings
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        var rocketOrigin by remember { mutableStateOf(Offset(0f,0f)) }
        var tapOrigin by remember { mutableStateOf(Offset(0f, 0f)) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    tapOrigin = it.positionInWindow()
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        boardState.onAddFirework(offset + tapOrigin - rocketOrigin)
                    }
                }
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Place all of the queens on the board without threatening each other",
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 32.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Column(
                    Modifier
                        .border(4.dp, Color.Black)
                        .padding(4.dp)
                ) {
                    for (row in 0 until boardState.size) {
                        Row {
                            for (col in 0 until boardState.size) {
                                val square = boardState.board.square(row, col) ?: continue
                                Box(
                                    modifier = Modifier
                                        .size(boardState.squareSizeDp.dp)
                                        .background(
                                            when {
                                                boardState.overSquare == square -> overColor
                                                square.light -> lightColor
                                                else -> darkColor
                                            }
                                        )
                                        .onGloballyPositioned { layout ->
                                            boardState.onSquarePositioned(square, layout)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val paths = boardState.paths[row][col]
                                    paths.forEach { move ->
                                        if (boardState.showMoves || move.collision) {
                                            MoveIcon(
                                                square = square,
                                                move = move,
                                                multiple = paths.size > 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // raise the zIndex for the box so the dragged queens are above the board
                Box(Modifier.zIndex(10f)) {
                    // background so queens aren't clipped
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(CircleShape)
                            .background(gradientColor2, CircleShape)
                            .padding(2.dp)
                            .border(4.dp, gradientColor1, CircleShape)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(2.dp)
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(boardState.squareSizeDp.dp)
                                .onGloballyPositioned {
                                    boardState.onDragOriginPositioned(it)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            boardState.queens.forEachIndexed { index, nQueen ->
                                QueenToken(
                                    boardState, nQueen, index
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = boardState.availableQueens.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned {
                        rocketOrigin = it.positionInWindow()
                    }
            ) {
                boardState.projectileUpdateList.forEach {
                    when(it.type) {
                        ProjectileType.Rocket -> Rocket(it)
                        ProjectileType.Firework -> Firework(it)
                    }

                }
            }
        }
    }
}
