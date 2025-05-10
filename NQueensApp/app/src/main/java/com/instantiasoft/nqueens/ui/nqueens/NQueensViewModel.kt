package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.lifecycle.ViewModel
import com.instantiasoft.nqueens.extensions.distance
import com.instantiasoft.nqueens.model.NQueen
import com.instantiasoft.nqueens.model.Projectile
import com.instantiasoft.nqueens.model.ProjectileColor
import com.instantiasoft.nqueens.model.ProjectileType
import com.instantiasoft.nqueens.model.Square
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@HiltViewModel(assistedFactory = NQueensViewModel.NQueensViewModelFactory::class)
class NQueensViewModel @AssistedInject constructor(
    @Assisted val size: Int
) : ViewModel() {
    @AssistedFactory
    interface NQueensViewModelFactory {
        fun create(size: Int): NQueensViewModel
    }

    data class OffsetState(
        val src: Offset,
        val dst: Offset,
        val duration: Long,
        val elapsed: Long = 0,
        val millis: Long = System.currentTimeMillis()
    )

    data class BoardState(
        val setupSize: Int = 0,
        val size: Int = 0,
        val queens: List<NQueen> = emptyList(),
        val board: Array<Array<Square>> = emptyArray(),
        val queenUpdateMap: Map<Int, OffsetState> = emptyMap(),
        val projectileUpdateList: List<Projectile> = emptyList(),
        val squareSize: Int = 0,
        val squareSizeDp: Int = 0,
        val dragOrigin: Offset = Offset(0f, 0f),
        val overSquare: Square? = null,
        val onDragOriginPositioned: (LayoutCoordinates) -> Unit,
        val onDrag: (index: Int, offset: Offset) -> Unit,
        val onDragEnd: (index: Int) -> Unit,
        val onSquarePositioned: (Square, LayoutCoordinates) -> Unit,
        val onAddFirework: (Offset) -> Unit
    ) {
        val needsSetup: Boolean get() = setupSize != size
    }

    private val _boardState = MutableStateFlow(
        BoardState(
            onDragOriginPositioned = ::onDragOriginPositioned,
            onDrag = ::onDrag,
            onDragEnd = ::onDragEnd,
            onSquarePositioned = ::onSquarePositioned,
            onAddFirework = ::onAddFireWork
        )
    )
    val boardState = _boardState.asStateFlow()

    init {
        _boardState.update {
            it.copy(
                size = size,
                queens = List(size) { NQueen() },
                board = Array(size) { row ->
                    Array(size) { col ->
                        Square(row = row, col = col, light = Square.isLight(row, col))
                    }
                }
            )
        }
    }

    fun setup(widthDp: Int, heightDp: Int) {
        if (boardState.value.size == 0) return

        val sizeDp = if (widthDp < heightDp) {
            widthDp
        } else {
            heightDp
        }

        val squareSizeDp = sizeDp / boardState.value.size
        _boardState.update { state ->
            state.copy(
                squareSizeDp = squareSizeDp.takeIf { it < MAX_SQUARE_SIZE } ?: MAX_SQUARE_SIZE
            )
        }
    }

    fun onDragOriginPositioned(layout: LayoutCoordinates) {
        val offset = layout.positionInWindow()
        if (offset == boardState.value.dragOrigin) return
        _boardState.update {
            it.copy(
                dragOrigin = offset
            )
        }
    }

    fun onSquarePositioned(square: Square, layout: LayoutCoordinates) {
        val position = layout.positionInWindow()
        val size = layout.size

        if (square.position == position && square.size == size) return

        val board = boardState.value.board.copyOf()
        board[square.row][square.col] = square.copy(
            position = position,
            size = size
        )

        _boardState.update {
            it.copy(
                board = board,
                squareSize = layout.size.width
            )
        }
    }

    fun onDragEnd(index: Int) {
        // move to center of square
        val queen = boardState.value.queens.getOrNull(index) ?: return

        val overSquare = boardState.value.overSquare
        val src = Offset(queen.x, queen.y)
        val dst = overSquare?.takeIf { it.piece == null }?.let {
            it.position - boardState.value.dragOrigin
        } ?: Offset(0f, 0f)

        _boardState.update { state ->
            state.copy(
                queenUpdateMap = state.queenUpdateMap.toMutableMap().apply {
                    this[index] = OffsetState(
                        src = src,
                        dst = dst,
                        duration = (src.distance(dst) * VELOCITY).toLong(),
                        elapsed = 0,
                        millis = System.currentTimeMillis()
                    )
                },
                overSquare = null,
                board = overSquare?.let { square ->
                    state.board.copyOf().apply {
                        this[overSquare.row][overSquare.col] = square.copy(
                            piece = queen
                        )
                    }
                } ?: state.board,
                projectileUpdateList = state.projectileUpdateList.toMutableList().apply {
                    repeat(5) {
                        this.add(
                            Projectile(
                                type = ProjectileType.Rocket,
                                offset = Offset.Zero,
                                color = ProjectileColor.get(Random.nextDouble()),
                                duration = (Random.nextDouble().toFloat() * 500 + 1500).toLong(),
                                velocityX = Random.nextDouble().toFloat() * 800f - 400f,
                                velocityY = Random.nextDouble().toFloat() * 1300f + 1800f,
                            )
                        )
                    }
                }
            )
        }
    }

    fun onDrag(index: Int, offset: Offset) {
        val state = boardState.value

        val queen = state.queens.getOrNull(index)?.let {
            it.copy(
                x = it.x + offset.x,
                y = it.y + offset.y
            )
        } ?: return

        val board = state.board

        // used the center of the dragged item to determine which square is covered
        val dragPosX = state.dragOrigin.x + state.squareSize/2 + queen.x
        val dragPosY = state.dragOrigin.y + state.squareSize/2 + queen.y

        var overSquare: Square? = null

        run testOverSquare@{
            board.forEach { row ->
                row.forEach { square ->
                    if (square.position.x < dragPosX && square.position.y < dragPosY) {
                        if (dragPosX < square.position.x + square.size.width && dragPosY < square.position.y + square.size.height) {
                            overSquare = square
                            return@testOverSquare
                        }
                    }
                }
            }
        }

        _boardState.update {
            it.copy(
                overSquare = overSquare,
                queens = it.queens.toMutableList().apply {
                    this[index] = queen
                }
            )
        }
    }

    fun updateAnimations() {
        updateQueens()
        updateProjectiles()
    }

    fun updateQueens() {
        if (boardState.value.queenUpdateMap.isEmpty()) return

        val millis = System.currentTimeMillis()

        val updateMap = boardState.value.queenUpdateMap.toMutableMap()
        val queens = boardState.value.queens.toMutableList()

        updateMap.keys.forEach { key ->
            val value = updateMap[key] ?: return@forEach

            queens.getOrNull(key)?.let {
                val elapsed = value.elapsed + millis - value.millis
                val offset = if (elapsed >= value.duration) {
                    updateMap.remove(key)
                    value.dst
                } else {
                    updateMap[key] = value.copy(elapsed = elapsed, millis = millis)
                    androidx.compose.ui.geometry.lerp(value.src, value.dst, elapsed.toFloat()/value.duration)
                }

                queens[key] = queens[key].copy(
                    x = offset.x, y = offset.y
                )
            }
        }

        _boardState.update {
            it.copy(
                queens = queens,
                queenUpdateMap = updateMap
            )
        }
    }

    fun updateProjectiles() {
        if (boardState.value.projectileUpdateList.isEmpty()) return

        val millis = System.currentTimeMillis()

        val projectiles = mutableListOf<Projectile>()

        val updateList = boardState.value.projectileUpdateList.toMutableList()
        val iterator = updateList.listIterator()
        while(iterator.hasNext()) {
            val projectile = iterator.next()

            val elapsedMillis = millis - projectile.millis
            val elapsed = elapsedMillis.toFloat() / 1000
            val offsetX = projectile.offset.x + projectile.velocityX * elapsed
            val offsetY = projectile.offset.y - projectile.velocityY * elapsed

            val velocityY = projectile.velocityY - elapsed * when(projectile.type) {
                ProjectileType.Firework -> FIREWORK_GRAVITY_PER_SECOND
                else -> GRAVITY_PER_SECOND
            }

            if (projectile.elapsed + elapsedMillis > projectile.duration + projectile.fadeDuration) {
                if (projectile.type == ProjectileType.Rocket) {
                    projectiles.addAll(addFireWorks(projectile.offset, projectile.color))
                }
                iterator.remove()
            } else {
                iterator.set(
                    projectile.copy(
                        elapsed = projectile.elapsed + elapsedMillis,
                        offset = Offset(offsetX, offsetY),
                        velocityX = if (projectile.type == ProjectileType.Firework) projectile.velocityX * 0.95f else projectile.velocityX,
                        velocityY = if (projectile.type == ProjectileType.Firework) velocityY * 0.95f else velocityY,
                        velocityZ = if (projectile.type == ProjectileType.Firework) projectile.velocityX * 0.95f else projectile.velocityZ,
                        millis = millis
                    )
                )
            }
        }

        updateList.addAll(projectiles)

        _boardState.update {
            it.copy(
                projectileUpdateList = updateList
            )
        }
    }

    fun onAddFireWork(offset: Offset, projectileCount: Int = 50) {
        _boardState.update {
            it.copy(
                projectileUpdateList = it.projectileUpdateList.plus(addFireWorks(offset, null, projectileCount))
            )
        }
    }

    fun addFireWorks(
        offset: Offset,
        color: ProjectileColor? = null,
        projectileCount: Int = 50
    ): List<Projectile> {
        val projectiles = mutableListOf<Projectile>()
        repeat(projectileCount) {
            val theta = Random.nextDouble() * 2 * Math.PI
            val u = Random.nextDouble() * 2 - 1
            val phi = acos(u)

            val radius = Random.nextDouble().toFloat() * BASE_FIREWORK_VELOCITY + RANDOM_FIREWORK_VELOCITY
            val x = radius * sin(phi) * cos(theta)
            val y = radius * sin(phi) * sin(theta)
            val z = radius * cos(phi)

            projectiles.add(
                Projectile(
                    type = ProjectileType.Firework,
                    duration = FIREWORK_DURATION,
                    fadeDuration = FIREWORK_FADE_DURATION,
                    color = color ?: ProjectileColor.get(Random.nextDouble()),
                    velocityX = x.toFloat(),
                    velocityY = y.toFloat(),
                    velocityZ = z.toFloat(),
                    offset = offset
                )
            )
        }

        return projectiles
    }

    companion object {
        const val GRAVITY_PER_SECOND = 2000
        const val FIREWORK_GRAVITY_PER_SECOND = 500
        const val BASE_FIREWORK_VELOCITY = 800
        const val RANDOM_FIREWORK_VELOCITY = 200
        const val FIREWORK_DURATION = 1200L
        const val FIREWORK_FADE_DURATION = 500L
        const val MAX_SQUARE_SIZE = 80
        const val VELOCITY = 500/1200f // 500 millis per 1200 pixels
    }
}