package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    boardState: NQueensViewModel.BoardState,
    boardActions: NQueensViewModel.BoardActions,
    sheetState: SheetState,
    onDismissRequest: () -> Unit
) {
    val minSize = 4
    val maxSize = 16
    val steps = maxSize - minSize

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Settings",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            var sliderPosition by remember { mutableFloatStateOf((boardState.size - minSize).coerceIn(0, steps).toFloat()/steps) }

            Text(
                text = "Board size: ${(sliderPosition * steps).roundToInt() + minSize}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it

                    val size = ((it * steps) + minSize).roundToInt().coerceIn(minSize, maxSize)
                    if (size != boardState.size) {
                        boardActions.onUpdateSize(size)
                    }
                },
                steps = steps,
                modifier = Modifier.fillMaxWidth().padding(32.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Show moves:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Switch(
                    checked = boardState.showMoves,
                    onCheckedChange = {
                        boardActions.onUpdateShowMoves(it)
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}