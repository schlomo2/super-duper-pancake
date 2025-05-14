package com.instantiasoft.nqueens.ui.nqueens

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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    val boardActions by queensViewModel.boardActions.collectAsStateWithLifecycle()
    val padding = 16
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(boardState) {
        if (boardState.needsSetup) {
            queensViewModel.setup(config.screenWidthDp-padding*2, config.screenHeightDp-padding*2)
        }
    }

    NQueensScreen(
        boardState = boardState,
        boardActions = boardActions,
        onSettings = {
            coroutineScope.launch {
                settingsSheetState.show()
            }
        }
    )

    if (settingsSheetState.isVisible) {
        SettingsSheet(
            boardState = boardState,
            boardActions = boardActions,
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
    boardActions: NQueensViewModel.BoardActions,
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
                        visible = boardState.playing,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Box(modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black)
                            .widthIn(min = 120.dp)
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(boardState.playingMillis / 1000)}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 40.sp
                            )
                        }
                    }
                }
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
                                            boardActions.onSquarePositioned(square, layout)
                                        }
                                        .pointerInput(boardState) {
                                            detectTapGestures {
                                                boardActions.onAddQueen(square)
                                            }
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
                                    boardActions.onDragOriginPositioned(it)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            boardState.queens.forEachIndexed { index, nQueen ->
                                QueenToken(
                                    boardState, boardActions, nQueen, index
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = boardState.availableQueens.toString(),
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
                visible = boardState.complete || !boardState.playing,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(Modifier
                    .fillMaxSize()
                    .background(Color(if (boardState.complete) 0x44000000 else 0x11000000))
                    .onGloballyPositioned {
                        tapOrigin = it.positionInWindow()
                    }
                    .pointerInput(boardState) {
                        detectTapGestures { offset ->
                            if (boardState.complete) {
                                boardActions.onAddFirework(offset + tapOrigin - rocketOrigin)
                            } else {
                                boardActions.onRestart()
                            }
                        }
                    }
                ) {
                    if (!boardState.complete) {
                        Box(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(10.dp, Color.White, RoundedCornerShape(16.dp))
                                .background(color = darkColor)
                                .padding(horizontal = 32.dp, vertical = 32.dp)
                                .align(Alignment.TopCenter),
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

            androidx.compose.animation.AnimatedVisibility(
                visible = boardState.complete,
                enter = slideInVertically { -it*2 },
                exit = slideOutVertically { -it*2 }
            ) {
                SuccessPanel(
                    boardState = boardState,
                    boardActions = boardActions,
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

@Composable
fun SuccessPanel(
    boardState: NQueensViewModel.BoardState,
    boardActions: NQueensViewModel.BoardActions,
    contentColor: Color,
    backgroundColor: Color
) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 32.dp)
        .clickable {
            boardActions.onRestart()
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
                text = "${boardState.playingMillis/1000} seconds",
                color = contentColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

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
        boardState = NQueensViewModel.updateSize(8).copy(squareSizeDp = 40),
        boardActions = NQueensViewModel.BoardActions(
            {},{_,_->},{},{_,_->},{},{},{},{},{},{}
        ),
        onSettings = {}
    )
}
