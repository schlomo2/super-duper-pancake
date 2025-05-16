package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.instantiasoft.nqueens.data.model.Board
import com.instantiasoft.nqueens.data.model.ProjectileType
import com.instantiasoft.nqueens.data.model.Square
import com.instantiasoft.nqueens.ui.fireworks.Firework
import com.instantiasoft.nqueens.ui.fireworks.Rocket
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NQueensScreen(
    queensViewModel: NQueensViewModel = hiltViewModel()
) {
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val coroutineScope = rememberCoroutineScope()

    val gameState by queensViewModel.gameState.collectAsStateWithLifecycle()
    val playState by queensViewModel.playState.collectAsStateWithLifecycle()
    val boardState by queensViewModel.boardState.collectAsStateWithLifecycle()
    val gameActions by queensViewModel.gameActions.collectAsStateWithLifecycle()
    val animationsState by queensViewModel.animationsState.collectAsStateWithLifecycle()
    val padding = 16
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(boardState) {
        if (boardState.needsSetup) {
            queensViewModel.setup(
                with(density) { (windowInfo.containerSize.width - padding * 2).toDp() }.value.toInt(),
                with(density) { (windowInfo.containerSize.height - padding * 2).toDp() }.value.toInt()
            )
        }
    }

    NQueensScreen(
        gameState = gameState,
        playState = playState,
        boardState = boardState,
        boardActions = gameActions,
        animationsState = animationsState,
        onSettings = {
            coroutineScope.launch {
                settingsSheetState.show()
            }
        }
    )

    if (settingsSheetState.isVisible) {
        SettingsSheet(
            gameState = gameState,
            boardState = boardState,
            boardActions = gameActions,
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
    gameState: NQueensViewModel.GameState,
    playState: NQueensViewModel.PlayState,
    boardState: NQueensViewModel.BoardState,
    boardActions: NQueensViewModel.GameActions,
    animationsState: NQueensViewModel.AnimationsState,
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
            navigationIcon = {
                IconButton(
                    onClick = {
                        boardActions.onRestart()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Restart",
                        modifier = Modifier.graphicsLayer {
                            this.rotationY = 180f
                        }
                    )
                }
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
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier
                    .heightIn(80.dp)
                    .fillMaxWidth(),
                    contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = playState.playing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier.heightIn(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                playState.bestTimes.times[boardState.size]?.let {
                                    Text(
                                        text = "BEST: ${(it / 1000)}",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black)
                                    .widthIn(min = 120.dp)
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${(playState.playingMillis / 1000)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 40.sp
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier.heightIn(min = 120.dp)
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        playState.playing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = "Place all of the queens on the board without threatening each other",
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 32.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }

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
                                                gameState.overSquare == square -> overColor
                                                square.light -> lightColor
                                                else -> darkColor
                                            }
                                        )
                                        .onGloballyPositioned { layout ->
                                            boardActions.onSquarePositioned(square, layout)
                                        }
                                        .pointerInput(boardState) {
                                            detectTapGestures {
                                                boardActions.onAddQueen(square)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    gameState.paths.getSquarePath(row, col)?.let { paths ->
                                        paths.forEach { move ->
                                            if (gameState.showMoves || move.collision) {
                                                MoveIcon(
                                                    dragIndex = gameState.dragIndex,
                                                    square = square,
                                                    move = move,
                                                    multipleMovesForSquare = paths.size > 1
                                                )
                                            }
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
                                    boardActions.onDragOriginPositioned(it)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            gameState.queens.forEachIndexed { index, nQueen ->
                                QueenToken(
                                    gameState, boardState, boardActions, nQueen, index
                                )
                            }

                            if (playState.complete) {
                                DoneToken(boardState)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = gameState.availableQueens.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(40.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = playState.complete || !playState.playing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(Modifier
                    .fillMaxSize()
                    .background(Color(0x88000000))
                    .onGloballyPositioned {
                        tapOrigin = it.positionInWindow()
                    }
                    .pointerInput(playState) {
                        detectTapGestures { offset ->
                            if (playState.complete) {
                                boardActions.onAddFirework(offset + tapOrigin - rocketOrigin)
                            } else {
                                boardActions.onRestart()
                            }
                        }
                    }
                ) {
                    if (!playState.complete) {
                        Column(
                            modifier = Modifier.align(Alignment.TopCenter),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Place all of the queens on the board without threatening each other",
                                modifier = Modifier.padding(horizontal = 32.dp, vertical = 32.dp),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(10.dp, Color.White, RoundedCornerShape(16.dp))
                                    .background(color = darkColor)
                                    .padding(horizontal = 32.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tap to start",
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = playState.complete,
                enter = slideInVertically { -it*2 },
                exit = slideOutVertically { -it*2 }
            ) {
                SuccessPanel(
                    playState = playState,
                    boardState = boardState,
                    gameActions = boardActions,
                    contentColor = Color.White,
                    backgroundColor = darkColor
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onGloballyPositioned {
                        rocketOrigin = it.positionInWindow()
                    }
            ) {
                animationsState.projectileUpdateList.forEach {
                    when(it.type) {
                        ProjectileType.Rocket -> Rocket(it)
                        ProjectileType.Firework -> Firework(it)
                    }

                }
            }
        }
    }
}

@Composable
fun SuccessPanel(
    playState: NQueensViewModel.PlayState,
    boardState: NQueensViewModel.BoardState,
    gameActions: NQueensViewModel.GameActions,
    contentColor: Color,
    backgroundColor: Color
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 32.dp)
        .clickable {
            gameActions.onRestart()
        },
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .border(10.dp, contentColor, RoundedCornerShape(16.dp))
            .background(color = backgroundColor)
            .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SUCCESS",
                color = contentColor,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "You placed ${boardState.size} Queens in",
                color = contentColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${playState.playingMillis/1000} seconds.",
                color = contentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            if (playState.prevMillis > 0) {
                Spacer(Modifier.height(24.dp))

                Text(
                    text = "The new BEST TIME!",
                    color = contentColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "(Click this panel to try again)",
                color = contentColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview
@Composable
fun NQueensScreenPreview() {
    NQueensScreen(
        gameState = NQueensViewModel.GameState(),
        playState = NQueensViewModel.PlayState(),
        boardState = NQueensViewModel.BoardState(size = 8, squareSizeDp = 40, board = Board(
            Array(8) { row ->
                Array(8) { col ->
                    Square(row = row, col = col, light = Square.isLight(row, col))
                }
            }
        )),
        boardActions = NQueensViewModel.GameActions(
            {},{_,_->},{},{_,_->},{},{},{},{},{},{}
        ),
        animationsState = NQueensViewModel.AnimationsState(),
        onSettings = {}
    )
}
