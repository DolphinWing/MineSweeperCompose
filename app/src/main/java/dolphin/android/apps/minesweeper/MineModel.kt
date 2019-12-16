package dolphin.android.apps.minesweeper

import android.util.SparseArray
import android.util.SparseIntArray
import androidx.compose.Model
import androidx.core.util.containsKey
import androidx.core.util.forEach
import kotlin.random.Random

@Model
object MineModel {
    private const val MINED = -99

    var state: GameState = GameState.Review

    var row: Int = 10
    var column: Int = 6
    var mines: Int = 10
    private var mapSize = this.row * this.column
    val mineMap = SparseIntArray()
    val stateMap = SparseArray<BlockState>()
    var loading: Boolean = false
    var markedMines: Int = 0

    fun generateMineMap() {
        loading = false
        mineMap.clear()
        mapSize = row * column
        markedMines = 0 //reset counter

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
            if (!isMine(index)) {//check 8-directions
                var count = 0
                if (!isFirstRow(index)) {
                    if (!isFirstColumn(index) && isMine(toNorthWestIndex(index))) ++count
                    if (isMine(toNorthIndex(index))) ++count
                    if (!isLastColumn(index) && isMine(toNorthEastIndex(index))) ++count
                }
                if (!isFirstColumn(index) && isMine(toWestIndex(index))) ++count
                if (!isLastColumn(index) && isMine(toEastIndex(index))) ++count
                if (!isLastRow(index)) {
                    if (!isFirstColumn(index) && isMine(toSouthWestIndex(index))) ++count
                    if (isMine(toSouthIndex(index))) ++count
                    if (!isLastColumn(index) && isMine(toSouthEastIndex(index))) ++count
                }
                mineMap.put(index, count)
            }
            stateMap.put(index, BlockState.None)
        }
        state = GameState.Start
        loading = true
    }

    fun toIndex(row: Int, column: Int) = row * this.column + column

    private fun isMine(index: Int): Boolean = if (index in 0 until mapSize)
        mineMap[index] == MINED else false

    private fun isMine(row: Int, column: Int) = isMine(toIndex(row, column))

    private fun toRow(index: Int): Int = index / this.column
    private fun toColumn(index: Int): Int = index % this.column
    private fun isFirstRow(index: Int): Boolean = toRow(index) == 0
    private fun isLastRow(index: Int): Boolean = toRow(index) == (this.row - 1)
    private fun isFirstColumn(index: Int): Boolean = toColumn(index) == 0
    private fun isLastColumn(index: Int): Boolean = toColumn(index) == (this.column - 1)

    private fun toNorthWestIndex(index: Int): Int = toWestIndex(toNorthIndex(index))
    private fun toNorthIndex(index: Int): Int = index - this.column
    private fun toNorthEastIndex(index: Int): Int = toEastIndex(toNorthIndex(index))
    private fun toWestIndex(index: Int): Int = index - 1
    private fun toEastIndex(index: Int): Int = index + 1
    private fun toSouthWestIndex(index: Int): Int = toWestIndex(toSouthIndex(index))
    private fun toSouthIndex(index: Int): Int = index + this.column
    private fun toSouthEastIndex(index: Int): Int = toEastIndex(toSouthIndex(index))

    fun changeState(row: Int, column: Int, state: BlockState) {
        loading = false
        stateMap.put(toIndex(row, column), state)
        loading = true
    }

    private fun isClosed(index: Int): Boolean = stateMap[index] == BlockState.None

    fun markAsMine(row: Int, column: Int): GameState {
        if (state == GameState.Review) return GameState.Review
        changeState(row, column, BlockState.Marked)
        state = if (verifyMineClear()) GameState.Cleared else GameState.Running
        return state
    }

    /**
     * Possible scenario: mark all mines or leave all mines
     */
    private fun verifyMineClear(): Boolean {
        var count = 0
        var notClicked = mapSize
        stateMap.forEach { key, value ->
            if (value == BlockState.Marked && isMine(key)) count++
            if (value != BlockState.None) notClicked--
        }
        return count == mines || notClicked == mines
    }

    fun stepOn(row: Int, column: Int): GameState {
        //if (state == GameState.Review) return GameState.Review
        state = if (isMine(row, column)) {
            stateMap.forEach { key, _ ->
                if (isMine(key)) stateMap.put(key, BlockState.Hidden)
            }
            changeState(row, column, BlockState.Mined)
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
        if (isClosed(index)) {
            stepOn(toRow(index), toColumn(index))
        }
    }

    private fun autoClick0(index: Int) {
        if (!isFirstRow(index)) stepOn0(toNorthIndex(index))
        if (!isLastRow(index)) stepOn0(toSouthIndex(index))
        if (!isFirstColumn(index)) stepOn0(toWestIndex(index))
        if (!isLastColumn(index)) stepOn0(toEastIndex(index))
    }

    enum class GameState {
        Start, Running, Exploded, Cleared, Review
    }

    val running: Boolean
        get() = state == GameState.Running || state == GameState.Start

    enum class BlockState {
        None, Mined, Text, Marked, Hidden
    }
}