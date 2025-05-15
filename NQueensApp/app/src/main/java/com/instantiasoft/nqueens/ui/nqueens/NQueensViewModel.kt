package com.instantiasoft.nqueens.ui.nqueens

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instantiasoft.nqueens.data.model.BestTimes
import com.instantiasoft.nqueens.data.model.Board
import com.instantiasoft.nqueens.data.model.Collision
import com.instantiasoft.nqueens.data.model.Move
import com.instantiasoft.nqueens.data.model.MoveDirection
import com.instantiasoft.nqueens.data.model.NQueen
import com.instantiasoft.nqueens.data.model.Paths
import com.instantiasoft.nqueens.data.model.Projectile
import com.instantiasoft.nqueens.data.model.ProjectileColor
import com.instantiasoft.nqueens.data.model.ProjectileType
import com.instantiasoft.nqueens.data.model.Square
import com.instantiasoft.nqueens.data.preferences.AppDataStore
import com.instantiasoft.nqueens.extensions.distance
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@HiltViewModel
class NQueensViewModel @Inject constructor(
    private val appDataStore: AppDataStore
) : ViewModel() {
    data class OffsetState(
        val src: Offset,
        val dst: Offset,
        val duration: Long,
        val elapsed: Long = 0,
        val millis: Long = System.currentTimeMillis()-1,
        val square: Square? = null
    )

    data class GameState(
        val complete: Boolean = false,
        val playingMillis: Long = 0,
        val prevMillis: Long = 0,
        val playing: Boolean = false,
        val bestTimes: BestTimes = BestTimes()
    )

    private val _gameState = MutableStateFlow(GameState())
    val gameState = _gameState.asStateFlow()

    data class BoardState(
        val setupSize: Int = 0,
        val size: Int = 0,
        val queens: List<NQueen> = emptyList(),
        val board: Board = Board(),
        val paths: Paths = Paths(),
        val collisionMap: Map<Collision, List<MoveDirection>> = emptyMap(),
        val queenUpdateMap: Map<Int, OffsetState> = emptyMap(),
        val projectileUpdateList: List<Projectile> = emptyList(),
        val squareSize: Int = 0,
        val squareSizeDp: Int = 0,
        val dragOrigin: Offset = Offset.Zero,
        val overSquare: Square? = null,
        val moveQueenIndex: Int? = null,
        val dragIndex: Int? = null,
        val calculatePaths: Boolean = false,
        val availableQueens: Int = 0,
        val showMoves: Boolean = false
    ) {
        val needsSetup: Boolean get() = setupSize != size
    }

    data class BoardActions(
        val onDragOriginPositioned: (LayoutCoordinates) -> Unit,
        val onDrag: (index: Int, offset: Offset) -> Unit,
        val onDragEnd: (index: Int) -> Unit,
        val onSquarePositioned: (Square, LayoutCoordinates) -> Unit,
        val onAddFirework: (Offset) -> Unit,
        val onUpdateSize: (Int) -> Unit,
        val onUpdateShowMoves: (Boolean) -> Unit,
        val onRestart: () -> Unit,
        val onAddQueen: (Square) -> Unit,
        val onReturnQueen: (NQueen) -> Unit
    )

    private val _boardState = MutableStateFlow(BoardState())
    val boardState = _boardState.asStateFlow()

    private val _boardActions = MutableStateFlow(
        BoardActions(
            onDragOriginPositioned = ::onDragOriginPositioned,
            onDrag = ::onDrag,
            onDragEnd = ::onDragEnd,
            onSquarePositioned = ::onSquarePositioned,
            onAddFirework = ::onAddFirework,
            onUpdateSize = ::onUpdateSize,
            onUpdateShowMoves = ::onUpdateShowMoves,
            onRestart = ::onRestart,
            onAddQueen = ::onAddQueen,
            onReturnQueen = ::onReturnQueen
        )
    )
    val boardActions = _boardActions.asStateFlow()

    private var timerJob: Job? = null

    init {
        setupDataStore()
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
                setupSize = boardState.value.size,
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

        val updatedSquare = square.copy(
            position = position,
            size = size
        )

        _boardState.update {
            it.copy(
                board = boardState.value.board.updateSquare(updatedSquare),
                squareSize = layout.size.width
            )
        }
    }

    private fun addQueenAnimation(
        index: Int,
        src: Offset,
        dst: Offset,
    ) {
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
                }
            )
        }
    }

    fun onDragEnd(index: Int) {
        val queen = boardState.value.queens.getOrNull(index) ?: return

        val overSquare = boardState.value.overSquare
        val src = Offset(queen.x, queen.y)
        val dst = overSquare?.takeIf { it.pieceIndex == queen.index }?.let {
            it.position - boardState.value.dragOrigin
        } ?: Offset(0f, 0f)

        addQueenAnimation(index, src, dst)

        _boardState.update { state ->
            state.copy(
                overSquare = null,
                moveQueenIndex = null,
                dragIndex = null
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

        _boardState.update {
            it.copy(
                queens = it.queens.toMutableList().apply {
                    this[index] = queen
                },
                moveQueenIndex = index,
                dragIndex = index
            )
        }
    }

    private fun moveQueen(index: Int) {
        val queen = boardState.value.queens.getOrNull(index) ?: return

        val state = boardState.value
        val board = state.board

        // used the center of the dragged item to determine which square is covered
        val dragPosX = state.dragOrigin.x + state.squareSize/2 + queen.x
        val dragPosY = state.dragOrigin.y + state.squareSize/2 + queen.y

        val updateSquares = mutableListOf<Square>()

        val overSquare = board.overSquare(dragPosX, dragPosY)

        state.overSquare?.takeIf { !it.sameBoardPosition(overSquare) && it.pieceIndex == index }?.let {
            updateSquares.add(it.copy(pieceIndex = null))
        }

        _boardState.update { updateState ->
            updateState.copy(
                overSquare = overSquare?.takeIf { updateState.dragIndex != null }?.let {
                    if (it.pieceIndex == null) {
                        val updated = it.copy(pieceIndex = index)
                        updateSquares.add(updated)
                        updated
                    } else {
                        it
                    }
                },
                queens = updateState.queens.toMutableList().apply {
                    this[index] = if (overSquare?.pieceIndex == null) {
                        queen.copy(square = overSquare)
                    } else if (overSquare.pieceIndex != index) {
                        queen.copy(square = null)
                    } else {
                        queen
                    }
                },
                moveQueenIndex = null,
                board = if (updateSquares.size > 0) board.updateSquares(updateSquares) else updateState.board,
                calculatePaths = true
            )
        }
    }

    private fun calculatePaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val state = boardState.value

            val board = state.board
            val paths = Paths.getPathArray(state.size)

            val collisions = mutableMapOf<Collision, List<MoveDirection>>()

            state.queens.forEach { queen ->
                val square = queen.square?.copy(pieceIndex = queen.index) ?: return@forEach

                checkWest(board, paths, square, collisions)
                checkEast(board, paths, square, collisions)
                checkNorth(board, paths, square, collisions)
                checkSouth(board, paths, square, collisions)
                checkNorthWest(board, paths, square, collisions)
                checkNorthEast(board, paths, square, collisions)
                checkSouthEast(board, paths, square, collisions)
                checkSouthWest(board, paths, square, collisions)
            }

            val availableQueens = state.queens.filter {
                it.index != state.dragIndex && it.square == null
            }.size

            _boardState.update { updateState ->
                updateState.copy(
                    paths = Paths(paths),
                    collisionMap = collisions,
                    calculatePaths = false,
                    availableQueens = availableQueens
                )
            }

            if (!gameState.value.complete && collisions.isEmpty() && availableQueens == 0 && state.dragIndex == null) {
                _gameState.update {
                    it.copy(
                        complete = true
                    )
                }

                updateBestTime()
                stopTimer()
                onAddRockets(10)
            }
        }
    }

    private fun checkWest(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        val row = square.row
        var col = square.col - 1

        var collision = false
        while (col >= 0) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            col--
        }

        col++

        // add the squares now that the collision is determined
        while (col <= square.col) {
            if (col == square.col) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.West)
                    }
                }
            } else {
                paths[row][col] =
                    paths[row][col].plus(Move(square.pieceIndex, MoveDirection.West, collision = collision))
            }
            col++
        }
    }

    private fun checkEast(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        val row = square.row
        var col = square.col + 1

        var collision = false
        while (col < board.size) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            col++
        }

        col--

        // add the squares now that the collision is determined
        while (col >= square.col) {
            if (col == square.col) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.East)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.East, collision))
            }
            col--
        }
    }

    private fun checkNorth(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row - 1
        val col = square.col

        var collision = false
        while (row >= 0) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row--
        }

        row++

        // add the squares now that the collision is determined
        while (row <= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.North)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.North, collision))
            }
            row++
        }
    }

    private fun checkSouth(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row + 1
        val col = square.col

        var collision = false
        while (row < board.size) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row++
        }

        row--

        // add the squares now that the collision is determined
        while (row >= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.South)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.South, collision))
            }
            row--
        }
    }

    private fun checkNorthWest(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row - 1
        var col = square.col - 1

        var collision = false
        while (row >= 0 && col >= 0) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row--
            col--
        }

        row++
        col++

        // add the squares now that the collision is determined
        while (row <= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.NorthWest)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.NorthWest, collision))
            }
            row++
            col++
        }
    }

    private fun checkNorthEast(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row - 1
        var col = square.col + 1

        var collision = false
        while (row >= 0 && col < board.size) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row--
            col++
        }

        row++
        col--

        // add the squares now that the collision is determined
        while (row <= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.NorthEast)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.NorthEast, collision))
            }
            row++
            col--
        }
    }

    private fun checkSouthEast(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row + 1
        var col = square.col + 1

        var collision = false
        while (row < board.size && col < board.size) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row++
            col++
        }

        row--
        col--

        // add the squares now that the collision is determined
        while (row >= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.SouthEast)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.SouthEast, collision))
            }
            row--
            col--
        }
    }

    private fun checkSouthWest(
        board: Board,
        paths: Array<Array<List<Move>>>,
        square: Square,
        collisionMap: MutableMap<Collision, List<MoveDirection>>
    ) {
        var row = square.row + 1
        var col = square.col - 1

        var collision = false
        while (row < board.size && col >= 0) {
            if (board.hasPiece(row, col)) {
                collision = true
                break
            }

            row++
            col--
        }

        row--
        col++

        // add the squares now that the collision is determined
        while (row >= square.row) {
            if (row == square.row) {
                if (collision) {
                    Collision(row, col).apply {
                        collisionMap[this] =
                            (collisionMap[this] ?: emptyList()).plus(MoveDirection.SouthWest)
                    }
                }
            } else {
                paths[row][col] = paths[row][col].plus(Move(square.pieceIndex, MoveDirection.SouthWest, collision))
            }
            row--
            col++
        }
    }

    fun updateAnimations() {
        if (boardState.value.calculatePaths)
            calculatePaths()

        boardState.value.moveQueenIndex?.let {
            moveQueen(it)
        }

        updateQueens()
        updateProjectiles()
    }

    private fun updateQueens() {
        if (boardState.value.queenUpdateMap.isEmpty()) return

        val millis = System.currentTimeMillis()

        val updateMap = boardState.value.queenUpdateMap.toMutableMap()
        val queens = boardState.value.queens.toMutableList()
        var calculatePaths = false

        val iterator = updateMap.keys.toList().listIterator()
        while(iterator.hasNext()) {
            val key = iterator.next()
            val value = updateMap[key] ?: continue

            queens.getOrNull(key)?.let {
                val elapsed = value.elapsed + millis - value.millis
                val offset = if (elapsed >= value.duration) {
                    updateMap.remove(key)
                    calculatePaths = true
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
                queenUpdateMap = updateMap,
                calculatePaths = if (calculatePaths) true else it.calculatePaths
            )
        }
    }

    private fun updateProjectiles() {
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
                    projectiles.addAll(addFireworks(projectile.offset, projectile.color))
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

    fun onAddFirework(offset: Offset, projectileCount: Int = 50) {
        _boardState.update {
            it.copy(
                projectileUpdateList = it.projectileUpdateList.plus(
                    addFireworks(
                        offset,
                        null,
                        projectileCount
                    )
                )
            )
        }
    }

    private fun addFireworks(
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

    private fun onAddRockets(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repeat(count) {
                _boardState.update { state ->
                    state.copy(
                        projectileUpdateList = state.projectileUpdateList.toMutableList().apply {
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
                    )
                }

                delay((Random.nextDouble() * 200).toLong() + 200)
            }
        }
    }

    fun onUpdateSize(size: Int, updateDataStore: Boolean = true) {
        if (updateDataStore) {
            viewModelScope.launch(Dispatchers.IO) {
                appDataStore.setBoardSize(size)
            }
        }

        stopTimer()

        _boardState.update {
            updateSize(size)
        }
    }

    fun onUpdateShowMoves(show: Boolean, updateDataStore: Boolean = true) {
        if (updateDataStore) {
            viewModelScope.launch(Dispatchers.IO) {
                appDataStore.setShowMoves(show)
            }
        }

        _boardState.update {
            it.copy(
                showMoves = show
            )
        }
    }

    fun onRestart() {
        val queens = boardState.value.queens

        _gameState.update {
            it.copy(
                complete = false,
                playing = true
            )
        }

        resetTimer()

        queens.forEach {
            if (it.square != null) {
                onReturnQueen(it)
            }
        }
    }

    fun onAddQueen(square: Square) {
        if (square.pieceIndex != null) return

        val queen = boardState.value.queens.findLast { it.square == null } ?: return

        _boardState.update { updateState ->
            updateState.copy(
                queens = updateState.queens.toMutableList().apply {
                    this[queen.index] = queen.copy(square = square)
                },
                board = updateState.board.updateSquare(square.copy(pieceIndex = queen.index))
            )
        }

        val src = Offset(queen.x, queen.y)
        val dst = square.position - boardState.value.dragOrigin

        addQueenAnimation(queen.index, src, dst)
    }

    fun onReturnQueen(queen: NQueen) {
        val square = queen.square ?: return

        _boardState.update { updateState ->
            updateState.copy(
                queens = updateState.queens.toMutableList().apply {
                    this[queen.index] = queen.copy(square = null)
                },
                board = updateState.board.updateSquare(square.row, square.col, null)
            )
        }

        val src = Offset(queen.x, queen.y)
        val dst = Offset.Zero

        addQueenAnimation(queen.index, src, dst)
    }

    private fun resetTimer() {
        timerJob?.cancel()

        _gameState.update {
            it.copy(
                playingMillis = 0,
                playing = true
            )
        }

        timerJob = viewModelScope.launch(Dispatchers.IO) {
            while(true) {
                delay(1000)
                _gameState.update {
                    it.copy(
                        playingMillis = it.playingMillis + 1000,
                        prevMillis = 0
                    )
                }
            }
        }
    }

    private fun updateBestTime() {
        var prevMillis = 0L
        gameState.value.bestTimes.times[boardState.value.size]?.let {
            if (gameState.value.playingMillis >= it) {
                return
            }

            prevMillis = it
        }

        viewModelScope.launch(Dispatchers.IO) {
            val bestTimes = gameState.value.bestTimes
            appDataStore.setBestTimes(
                bestTimes.copy(
                    times = bestTimes.times.toMutableMap().apply {
                        this[boardState.value.size] = gameState.value.playingMillis
                    }
                )
            )
        }

        _gameState.update {
            it.copy(
                prevMillis = prevMillis
            )
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()

        _gameState.update {
            it.copy(playing = false)
        }
    }

    private fun setupDataStore() {
        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.boardSize.collect {
                if (boardState.value.size != it) {
                    onUpdateSize(it, false)
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.showMoves.collect {
                onUpdateShowMoves(it, false)
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            appDataStore.bestTimes.collect { times ->
                _gameState.update {
                    it.copy(bestTimes = times)
                }
            }
        }
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

        fun updateSize(size: Int): BoardState {
            return BoardState(
                size = size,
                queens = List(size) { NQueen(index = it) },
                availableQueens = size,
                board = Board(Array(size) { row ->
                    Array(size) { col ->
                        Square(row = row, col = col, light = Square.isLight(row, col))
                    }
                }),
                paths = Paths.getPaths(size)
            )
        }
    }
}