package com.instantiasoft.nqueens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.instantiasoft.nqueens.ui.nqueens.NQueensScreen
import com.instantiasoft.nqueens.ui.theme.NQueensAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NQueensAppTheme {
                NQueensScreen(
                    size = 8
                )
            }
        }
    }
}
