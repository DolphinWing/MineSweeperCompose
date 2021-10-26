package dolphin.android.apps.minesweeper

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.SparseIntArray
import androidx.compose.runtime.mutableStateOf
import kotlin.random.Random

/**
 * data model for app
 *
 * @param maxRows max rows of the map, depending on screen size
 * @param maxCols max columns of the map, depending on screen size
 * @param maxMines max mines in the map
 */
class MineModel(val maxRows: Int = 6, val maxCols: Int = 5, maxMines: Int = 10) {
    companion object {
        private const val TAG = "MineModel"
        private const val MINED = -99
    }

    /**
     * Current game state
     */
    var gameState = mutableStateOf(GameState.Start)

    /**
     * Play clock
     */
    var clock = mutableStateOf(0L)

    /**
     * Current rows of the map
     */
    var row = mutableStateOf(maxRows)

    /**
     * Current column of the map
     */
    var column = mutableStateOf(maxCols)

    /**
     * Current mines of the map
     */
    var mines = mutableStateOf(maxMines)

    /**
     * A flag indicates if the app is loading or not
     */
    var loading = mutableStateOf(false)

    /**
     * You can call it a debug mode or god mode.
     */
    var funny = mutableStateOf(BuildConfig.DEBUG) /* a funny mode for YA */

    /**
     * Current marked blocks as mines
     */
    var markedMines = 0
        set(value) {
            remainingMines.value = mines.value - value
            field = value
        }

    /**
     * Remember how many mines we have not found
     */
    var remainingMines = mutableStateOf(0)

    private val mineMap = SparseIntArray()

    private val mapSize: Int
        get() = this.row.value * this.column.value

    /**
     * block individual state
     */
    var blockState = Array(mapSize) { mutableStateOf(BlockState.None) }
        private set

    private var firstClick: Boolean = false

    /**
     * Game state
     */
    enum class GameState {
        Start, Running, Exploded, Cleared, Review, // Destroyed
    }

    /**
     * indicate if the game is running.
     */
    val running: Boolean
        get() = gameState.value == GameState.Running || gameState.value == GameState.Start

    enum class BlockState {
        None, Mined, Text, Marked, Hidden
    }

    init {
        generateMineMap()
    }

    /**
     * Create a new mine map.
     *
     * @param row target rows of the map
     * @param column target columns of the map
     * @param mines target mines of the map
     */
    fun generateMineMap(
        row: Int = this.row.value,
        column: Int = this.column.value,
        mines: Int = this.mines.value,
    ) {
        loading.value = true
        mineMap.clear()

        this.row.value = row
        this.column.value = column

        this.mines.value =
            if (mines > mapSize) mapSize else mines // ensure mines smaller than map size
        Log.v(TAG, "create ${row}x$column with ${this.mines.value} mines")

        markedMines = 0 // reset counter
        clock.value = 0L // reset clock

        putMinesIntoField()
        calculateField() // generateMineMap

        gameState.value = GameState.Start
        loading.value = false
        firstClick = true
    }

    private fun putMinesIntoField() {
        repeat(this.mines.value) {
            mineMap.put(randomNewMine(), MINED)
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
        blockState = Array(mapSize) { mutableStateOf(BlockState.None) }
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
                mineMap.put(index, count)
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
    fun toIndex(row: Int, column: Int) = row * this.column.value + column

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
        return mineMap[toIndex(row, column)]
    }

    private fun toRow(index: Int): Int = index / this.column.value
    private fun toColumn(index: Int): Int = index % this.column.value
    private fun notFirstRow(index: Int): Boolean = toRow(index) != 0
    private fun notLastRow(index: Int): Boolean = toRow(index) != (this.row.value - 1)
    private fun notFirstColumn(index: Int): Boolean = toColumn(index) != 0
    private fun notLastColumn(index: Int): Boolean = toColumn(index) != (this.column.value - 1)

    private fun toNorthWestIndex(index: Int): Int = toWestIndex(toNorthIndex(index))
    private fun toNorthIndex(index: Int): Int = index - this.column.value
    private fun toNorthEastIndex(index: Int): Int = toEastIndex(toNorthIndex(index))
    private fun toWestIndex(index: Int): Int = index - 1
    private fun toEastIndex(index: Int): Int = index + 1
    private fun toSouthWestIndex(index: Int): Int = toWestIndex(toSouthIndex(index))
    private fun toSouthIndex(index: Int): Int = index + this.column.value
    private fun toSouthEastIndex(index: Int): Int = toEastIndex(toSouthIndex(index))

    /**
     * Change block state
     *
     * @param row row index of the block
     * @param column column index of the block
     * @param state new block state
     */
    fun changeState(row: Int, column: Int, state: BlockState) {
        // loading = false
        blockState[toIndex(row, column)].value = state
        // loading = true
    }

    private fun isBlockNotOpen(index: Int): Boolean = when (blockState[index].value) {
        BlockState.None -> true
        else -> false
    }

    /**
     * Mark a block as a mine
     */
    fun markAsMine(row: Int, column: Int): GameState {
        if (gameState.value == GameState.Review) return GameState.Review
        firstClick = false // treat it as game started
        changeState(row, column, BlockState.Marked)
        markedMines++
        gameState.value = if (verifyMineClear()) GameState.Cleared else GameState.Running
        if (gameState.value == GameState.Cleared) Log.v(TAG, "You won!")
        return gameState.value
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
        mineMap.put(oldIndex, 0) // remove mine
        var newIndex = randomNewMine()
        while (newIndex == oldIndex) newIndex = randomNewMine()
        Log.v(TAG, "move $oldIndex to $newIndex")
        mineMap.put(newIndex, MINED)
    }

    private val clockHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (running) {
                clock.value++
                tick()
            }
        }

        fun tick() {
            sendEmptyMessageDelayed(0, 1000)
        }
    }

    /**
     * Click on a block
     *
     * @param row row index of the block
     * @param column column index of the block
     * @return game state after step on the block
     */
    fun stepOn(row: Int, column: Int): GameState {
        if (gameState.value == GameState.Review) return GameState.Review
        gameState.value = if (mineExists(row, column)) {
            if (firstClick) { // recalculate mine map because first click cannot be a mine
                Log.w(TAG, "recalculate mine map")
                loading.value = true
                moveMineToAnotherPlace(toIndex(row, column)) // move to another place
                calculateField() // recalculate map
                loading.value = false
                return stepOn(row, column) // step on again, won't be a mine
            } else {
                // reveal not marked mines
                blockState.forEachIndexed { key, block ->
                    if (mineExists(key) && block.value != BlockState.Marked) {
                        block.value = BlockState.Hidden
                    }
                }
                changeState(row, column, BlockState.Mined)
                Log.v(TAG, "Game over! you lost!!")
                GameState.Exploded
            }
        } else { // not mine, reveal it
            changeState(row, column, BlockState.Text)
            if (mineMap.get(toIndex(row, column)) == 0) { // auto click
                autoClick0(toIndex(row, column))
            }
            if (verifyMineClear()) GameState.Cleared else GameState.Running
        }
        if (firstClick) {
            clockHandler.tick()
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
            if (mineMap.get(index) == 0) { // auto click
                autoClick0(index)
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
        get() = row.value == 5 && column.value == 4 && mines.value == 5

    /**
     * Check if we are about to start funny mode or not
     *
     * @return true if funny mode is started
     */
    fun onTheWayToFunnyMode(): Boolean {
        if (soFunny && ++funnyCount == 10) {
            Log.w(TAG, "enable YA mode!")
            funny.value = true
        }
        return soFunny
    }
}
