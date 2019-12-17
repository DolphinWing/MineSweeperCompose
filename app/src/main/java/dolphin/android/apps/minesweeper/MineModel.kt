package dolphin.android.apps.minesweeper

import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import androidx.compose.Model
import androidx.core.util.containsKey
import androidx.core.util.forEach
import kotlin.random.Random

@Model
object MineModel {
    private const val TAG = "MineModel"
    private const val MINED = -99

    var state: GameState = GameState.Review
    var clock: Long = 0

    var row: Int = 10
    var column: Int = 6
    var mines: Int = 10
    private var mapSize = this.row * this.column
    val mineMap = SparseIntArray()
    val blockMap = SparseArray<BlockState>()
    var loading: Boolean = false
    var markedMines: Int = 0

    enum class GameState {
        Start, Running, Exploded, Cleared, Review
    }

    val running: Boolean
        get() = state == GameState.Running || state == GameState.Start

    enum class BlockState {
        None, Mined, Text, Marked, Hidden
    }

    fun generateMineMap() {
        loading = true
        mineMap.clear()
        mapSize = row * column
        markedMines = 0 //reset counter
        clock = 0 //reset clock

        /* calculate mines position */
        repeat(mines) {
            var i = Random.nextInt(0, mapSize)
            while (mineMap.containsKey(i)) {
                i = Random.nextInt(0, mapSize)
            }
            mineMap.put(i, MINED)
        }

        /* calculate mine count */
        repeat(mapSize) { index ->
            if (!mineExists(index)) {//check 8-directions
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
            blockMap.put(index, BlockState.None)
        }
        state = GameState.Start
        loading = false
    }

    fun toIndex(row: Int, column: Int) = row * this.column + column

    private fun mineExists(index: Int): Boolean = if (index in 0 until mapSize)
        mineMap[index] == MINED else false

    private fun mineExists(row: Int, column: Int) = mineExists(toIndex(row, column))

    private fun toRow(index: Int): Int = index / this.column
    private fun toColumn(index: Int): Int = index % this.column
    private fun notFirstRow(index: Int): Boolean = toRow(index) != 0
    private fun notLastRow(index: Int): Boolean = toRow(index) != (this.row - 1)
    private fun notFirstColumn(index: Int): Boolean = toColumn(index) != 0
    private fun notLastColumn(index: Int): Boolean = toColumn(index) != (this.column - 1)

    private fun toNorthWestIndex(index: Int): Int = toWestIndex(toNorthIndex(index))
    private fun toNorthIndex(index: Int): Int = index - this.column
    private fun toNorthEastIndex(index: Int): Int = toEastIndex(toNorthIndex(index))
    private fun toWestIndex(index: Int): Int = index - 1
    private fun toEastIndex(index: Int): Int = index + 1
    private fun toSouthWestIndex(index: Int): Int = toWestIndex(toSouthIndex(index))
    private fun toSouthIndex(index: Int): Int = index + this.column
    private fun toSouthEastIndex(index: Int): Int = toEastIndex(toSouthIndex(index))

    fun changeState(row: Int, column: Int, state: BlockState) {
        //loading = false
        blockMap.put(toIndex(row, column), state)
        //loading = true
    }

    private fun isBlockNotOpen(index: Int): Boolean = when (blockMap[index]) {
        BlockState.None -> true
        else -> false
    }

    fun markAsMine(row: Int, column: Int): GameState {
        if (state == GameState.Review) return GameState.Review
        changeState(row, column, BlockState.Marked)
        markedMines++
        state = if (verifyMineClear()) GameState.Cleared else GameState.Running
        if (state == GameState.Cleared) Log.v(TAG, "You won!")
        return state
    }

    /**
     * Possible scenario: mark all mines or leave all mines
     */
    private fun verifyMineClear(): Boolean {
        var count = 0
        //var notClicked = mapSize
        blockMap.forEach { key, value ->
            if (mineExists(key)) {
                if (value == BlockState.Marked) count++
                //if (value == BlockState.None) count++
            }
            //if (value != BlockState.None) notClicked--
        }
        return count == mines //|| notClicked == mines
    }

    fun stepOn(row: Int, column: Int): GameState {
        if (state == GameState.Review) return GameState.Review
        state = if (mineExists(row, column)) {
            //reveal not marked mines
            blockMap.forEach { key, _ ->
                if (mineExists(key) && blockMap[key] != BlockState.Marked) {
                    blockMap.put(key, BlockState.Hidden)
                }
            }
            changeState(row, column, BlockState.Mined)
            Log.v(TAG, "Game over! you lost!!")
            GameState.Exploded
        } else {
            changeState(row, column, BlockState.Text)
            if (mineMap[toIndex(row, column)] == 0) {//auto click
                autoClick0(toIndex(row, column))
            }
            if (verifyMineClear()) GameState.Cleared else GameState.Running
        }
        return state
    }

    private fun stepOn0(index: Int) {
        if (isBlockNotOpen(index)) {
            //stepOn(toRow(index), toColumn(index))
            blockMap.put(index, BlockState.Text)
            if (mineMap[index] == 0) {//auto click
                autoClick0(index)
            }
        }
    }

    private fun autoClick0(index: Int) {
        if (notFirstRow(index)) stepOn0(toNorthIndex(index))
        if (notLastRow(index)) stepOn0(toSouthIndex(index))
        if (notFirstColumn(index)) stepOn0(toWestIndex(index))
        if (notLastColumn(index)) stepOn0(toEastIndex(index))
    }

}