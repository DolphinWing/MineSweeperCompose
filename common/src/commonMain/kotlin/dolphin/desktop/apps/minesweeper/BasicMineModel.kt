package dolphin.desktop.apps.minesweeper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Basic mine model implementation.
 *
 * @param maxRows max rows
 * @param maxCols max columns
 * @param maxMines max mines
 */
abstract class BasicMineModel(maxRows: Int = 6, maxCols: Int = 5, maxMines: Int = 10) {
    companion object {
        private const val MINED = -99
    }

    abstract fun log(message: String)
    abstract fun startTicking()
    abstract fun stopTicking()

    /**
     * Current game state
     */
    val gameState = MutableStateFlow(GameState.Start)

    /**
     * Play clock
     */
    val clock = MutableStateFlow(0L)

    /**
     * Current rows of the map
     */
    val rows = MutableStateFlow(maxRows)

    /**
     * Current column of the map
     */
    val columns = MutableStateFlow(maxCols)

    /**
     * Current mines of the map
     */
    val mines = MutableStateFlow(maxMines)

    /**
     * A flag indicates if the app is loading or not
     */
    val loading = MutableStateFlow(false)

    /**
     * You can call it a debug mode or god mode.
     */
    val funny = MutableStateFlow(false) /* a funny mode for YA */

    /**
     * Current marked blocks as mines
     */
    private var markedMines = 0

    /**
     * Remember how many mines we have not found
     */
    val remainingMines = MutableStateFlow(0)

    private val mapSize: Int
        get() = this.rows.value * this.columns.value

    /**
     * block individual state
     */
    var blockState = Array(mapSize) { MutableStateFlow(BlockState.None) }
        private set

    private var firstClick: Boolean = false

    /**
     * indicate if the game is running.
     */
    val running: Boolean
        get() = gameState.value == GameState.Running || gameState.value == GameState.Start

    private val mineMap = HashMap<Int, Int>()

    /**
     * Create a new mine map.
     *
     * @param row target rows of the map
     * @param column target columns of the map
     * @param mine target mines of the map
     */
    suspend fun generateMineMap(
        row: Int = this.rows.value,
        column: Int = this.columns.value,
        mine: Int = this.mines.value,
    ) = withContext(Dispatchers.Default) {
        loading.emit(true)
        mineMap.clear()
        stopTicking() // generateMineMap
        blockState.forEach { block -> block.emit(BlockState.None) } // reset block state

        // ensure mines smaller than map size
        val mineCount = if (mine > mapSize) mapSize else mine
        log("create ${row}x$column with $mineCount mines")

        markedMines = 0 // reset counter
        rows.emit(row)
        columns.emit(column)
        mines.emit(mineCount)
        clock.emit(0) // reset clock
        remainingMines.emit(mineCount)

        putMinesIntoField(mineCount)
        calculateField() // generateMineMap

        gameState.emit(GameState.Start)
        loading.emit(false)
        firstClick = true
    }

    private fun putMinesIntoField(mineCount: Int = this.mines.value) {
        repeat(mineCount) {
            mineMap[randomNewMine()] = MINED
        }
    }

    private fun randomNewMine(): Int {
        var i = Random.nextInt(0, mapSize)
        while (mineExists(i)) {
            i = Random.nextInt(0, mapSize)
        }
        return i
    }

    private fun calculateField() {
        blockState = Array(mapSize) { MutableStateFlow(BlockState.None) }
        repeat(mapSize) { index ->
            if (!mineExists(index)) { // check 8-directions
                var count = 0
                if (notFirstRow(index)) {
                    if (notFirstColumn(index) && mineExists(toNorthWestIndex(index))) ++count
                    if (mineExists(toNorthIndex(index))) ++count
                    if (notLastColumn(index) && mineExists(toNorthEastIndex(index))) ++count
                }
                if (notFirstColumn(index) && mineExists(toWestIndex(index))) ++count
                if (notLastColumn(index) && mineExists(toEastIndex(index))) ++count
                if (notLastRow(index)) {
                    if (notFirstColumn(index) && mineExists(toSouthWestIndex(index))) ++count
                    if (mineExists(toSouthIndex(index))) ++count
                    if (notLastColumn(index) && mineExists(toSouthEastIndex(index))) ++count
                }
                mineMap[index] = count
            }
        }
    }

    /**
     * Convert block coordinates to array index.
     *
     * @param row row index of the block
     * @param column column index of the block
     * @return array index
     */
    fun toIndex(row: Int, column: Int) = row * this.columns.value + column

    private fun mineExists(index: Int): Boolean = if (index in 0 until mapSize)
        mineMap[index] == MINED else false

    private fun mineExists(row: Int, column: Int) = mineExists(toIndex(row, column))

    /**
     * Get block state
     *
     * @param row row index of the block
     * @param column column index of the block
     */
    fun getMineIndicator(row: Int, column: Int): Int {
        // Log.d(TAG, "==> ${toIndex(row, column)} ${mineMap[toIndex(row, column)]}")
        return mineMap[toIndex(row, column)] ?: 0
    }

    private fun toRow(index: Int): Int = index / this.columns.value
    private fun toColumn(index: Int): Int = index % this.columns.value
    private fun notFirstRow(index: Int): Boolean = toRow(index) != 0
    private fun notLastRow(index: Int): Boolean = toRow(index) != (this.rows.value - 1)
    private fun notFirstColumn(index: Int): Boolean = toColumn(index) != 0
    private fun notLastColumn(index: Int): Boolean = toColumn(index) != (this.columns.value - 1)

    private fun toNorthWestIndex(index: Int): Int = toWestIndex(toNorthIndex(index))
    private fun toNorthIndex(index: Int): Int = index - this.columns.value
    private fun toNorthEastIndex(index: Int): Int = toEastIndex(toNorthIndex(index))
    private fun toWestIndex(index: Int): Int = index - 1
    private fun toEastIndex(index: Int): Int = index + 1
    private fun toSouthWestIndex(index: Int): Int = toWestIndex(toSouthIndex(index))
    private fun toSouthIndex(index: Int): Int = index + this.columns.value
    private fun toSouthEastIndex(index: Int): Int = toEastIndex(toSouthIndex(index))

    /**
     * Change block state
     *
     * @param row row index of the block
     * @param column column index of the block
     * @param state new block state
     */
    private fun changeState(row: Int, column: Int, state: BlockState) {
        blockState[toIndex(row, column)].value = state
    }

    private fun isBlockNotOpen(index: Int): Boolean = when (blockState[index].value) {
        BlockState.None -> true
        else -> false
    }

    /**
     * Mark a block as a mine
     *
     * @param row row index of the block
     * @param column column index of the block
     */
    suspend fun markAsMineBlock(row: Int, column: Int) {
        if (running) {
            gameState.emit(markAsMine(row, column))
        } else {
            log("current game state: ${gameState.value}")
        }
    }

    /**
     * Mark a block as a mine
     *
     * @param row row index of the block
     * @param column column index of the block
     */
    private suspend fun markAsMine(row: Int, column: Int): GameState {
        if (gameState.value == GameState.Review) return GameState.Review
        if (markedMines >= mines.value) {
            log("too many mines!!!")
            return gameState.value
        }
        firstClick = false // treat it as game started
        changeState(row, column, BlockState.Marked)
        markedMines++
        remainingMines.emit(this.mines.value - markedMines)
        gameState.emit(if (verifyMineClear()) GameState.Cleared else GameState.Running)
        if (gameState.value == GameState.Cleared) log("You won!")
        return gameState.value
    }

    /**
     * Unmark the block as a mine
     *
     * @param row row index of the block
     * @param column column index of the block
     */
    suspend fun unmarkMine(row: Int, column: Int) {
        if (running) {
            changeState(row, column, BlockState.None)
            --markedMines
            remainingMines.emit(this.mines.value - markedMines)
        } else {
            log("not running")
        }
    }

    /**
     * Possible scenario: mark or leave all mines
     */
    private fun verifyMineClear(): Boolean {
        val suspects = ArrayList<Int>()
        blockState.forEachIndexed { key, state ->
            if (state.value == BlockState.None || state.value == BlockState.Marked) suspects.add(key)
        }
        // Log.d(TAG, "verify mines: ${suspects.size} ${suspects.none { !mineExists(it) }}")
        return suspects.none { !mineExists(it) }
    }

    private fun moveMineToAnotherPlace(oldIndex: Int) {
        mineMap[oldIndex] = 0 // remove mine
        var newIndex = randomNewMine()
        while (newIndex == oldIndex) newIndex = randomNewMine()
        log("move mine @ $oldIndex to $newIndex")
        mineMap[newIndex] = MINED
    }


    suspend fun stepOnBlock(row: Int, column: Int) {
        if (running) {
            gameState.emit(stepOn(row, column))
        } else {
            log("current game state: ${gameState.value}")
        }
    }

    /**
     * Click on a block
     *
     * @param row row index of the block
     * @param column column index of the block
     * @return game state after step on the block
     */
    private suspend fun stepOn(row: Int, column: Int): GameState {
        // log("step on ($row, $column)")
        if (gameState.value == GameState.Review) return GameState.Review
        val state = if (mineExists(row, column)) {
            if (firstClick) { // recalculate mine map because first click cannot be a mine
                log("recalculate mine map")
                loading.emit(true)
                moveMineToAnotherPlace(toIndex(row, column)) // move to another place
                calculateField() // recalculate map
                loading.emit(false)
                return stepOn(row, column) // step on again, won't be a mine
            } else {
                // reveal not marked mines
                blockState.filterIndexed { key, block ->
                    mineExists(key) && block.value != BlockState.Marked
                }.forEach { block ->
                    block.value = BlockState.Mined
                }
                changeState(row, column, BlockState.Mined)
                stopTicking() // GameState.Exploded
                log("Game over! you lost!!")
                GameState.Exploded
            }
        } else { // not mine, reveal it
            changeState(row, column, BlockState.Text)
            if (mineMap[toIndex(row, column)] == 0) { // auto click
                autoClick0(toIndex(row, column)) // stepOn
            }
            if (verifyMineClear()) {
                stopTicking() // GameState.Cleared
                GameState.Cleared
            } else GameState.Running
        }
        gameState.emit(state)
        if (firstClick) {
            startTicking()
            firstClick = false // mark that we have click at least one block
        }
        return gameState.value
    }

    /**
     * Internal step on action.
     *
     * @param index array index
     */
    private fun stepOn0(index: Int) {
        if (isBlockNotOpen(index)) {
            blockState[index].value = BlockState.Text
            if (mineMap[index] == 0) { // auto click
                autoClick0(index) // stepOn0
            }
        }
    }

    /**
     * Internal block auto click action
     *
     * @param index array index
     */
    private fun autoClick0(index: Int) {
        if (notFirstRow(index) && notFirstColumn(index)) stepOn0(toNorthWestIndex(index))
        if (notFirstRow(index)) stepOn0(toNorthIndex(index))
        if (notFirstRow(index) && notLastColumn(index)) stepOn0(toNorthEastIndex(index))
        if (notFirstColumn(index)) stepOn0(toWestIndex(index))
        if (notLastColumn(index)) stepOn0(toEastIndex(index))
        if (notLastRow(index) && notFirstColumn(index)) stepOn0(toSouthWestIndex(index))
        if (notLastRow(index)) stepOn0(toSouthIndex(index))
        if (notLastRow(index) && notLastColumn(index)) stepOn0(toSouthEastIndex(index))
    }

    private var funnyCount = 0
    private val soFunny: Boolean
        get() = rows.value == 5 && columns.value == 4 && mines.value == 5

    /**
     * Check if we are about to start funny mode or not
     *
     * @return true if funny mode is started
     */
    suspend fun onTheWayToFunnyMode(): Boolean {
        if (soFunny && ++funnyCount == 10) {
            log("enable YA mode!")
            funny.emit(true)
        }
        return soFunny
    }
}
