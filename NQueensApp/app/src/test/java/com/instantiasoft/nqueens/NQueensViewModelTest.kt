package com.instantiasoft.nqueens

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.instantiasoft.nqueens.data.model.BestTimes
import com.instantiasoft.nqueens.data.preferences.AppDataStore
import com.instantiasoft.nqueens.ui.nqueens.NQueensViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class NQueensViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var mockAppDataStore: AppDataStore

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockAppDataStore = mock()

        `when`(mockAppDataStore.showMoves).thenReturn(MutableStateFlow(false))
        `when`(mockAppDataStore.bestTimes).thenReturn(MutableStateFlow(BestTimes()))

    }

    fun getViewModel(
        mockBoardSize: StateFlow<Int> = MutableStateFlow(4),

    ): NQueensViewModel {
        `when`(mockAppDataStore.boardSize).thenReturn(mockBoardSize)

        return NQueensViewModel(mockAppDataStore, testDispatcher)
    }

    @Test
    fun `success on 4x4 board`() = testScope.runTest {
        val sut = getViewModel()

        advanceUntilIdle()

        val boardState = sut.boardState.value
        assertEquals(4, boardState.size)

        sut.onAddQueen(boardState.board.square(1, 0)!!)
        assertEquals(3, sut.boardState.value.board.square(1, 0)?.pieceIndex)

        sut.onAddQueen(boardState.board.square(3, 1)!!)
        assertEquals(2, sut.boardState.value.board.square(3, 1)?.pieceIndex)

        sut.onAddQueen(boardState.board.square(2, 3)!!)
        assertEquals(1, sut.boardState.value.board.square(2, 3)?.pieceIndex)

        sut.onAddQueen(boardState.board.square(0, 2)!!)
        assertEquals(0, sut.boardState.value.board.square(0, 2)?.pieceIndex)

        assertFalse(sut.boardState.value.calculatePaths)
        sut.updateAnimations()

        advanceUntilIdle()

        assertTrue(sut.boardState.value.calculatePaths)

        sut.updateAnimations()

        advanceUntilIdle()

        assertEquals(0, sut.boardState.value.collisionMap.count())
        assertEquals(true, sut.gameState.value.complete)
    }

    @Test
    fun `collisions on 4x4 board`() = testScope.runTest {
        val sut = getViewModel()

        advanceUntilIdle()

        val boardState = sut.boardState.value
        assertEquals(4, boardState.size)

        sut.onAddQueen(boardState.board.square(1, 0)!!)
        assertEquals(3, sut.boardState.value.board.square(1, 0)?.pieceIndex)

        sut.onAddQueen(boardState.board.square(2, 0)!!)
        assertEquals(2, sut.boardState.value.board.square(2, 0)?.pieceIndex)

        assertFalse(sut.boardState.value.calculatePaths)
        sut.updateAnimations()

        advanceUntilIdle()

        assertTrue(sut.boardState.value.calculatePaths)

        sut.updateAnimations()

        advanceUntilIdle()

        assertEquals(2, sut.boardState.value.collisionMap.count())
    }
}