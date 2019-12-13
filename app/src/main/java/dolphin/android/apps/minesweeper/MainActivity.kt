package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.util.Log
import android.util.SparseIntArray
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.core.util.containsKey
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.core.gesture.PressGestureDetector
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.layout.Center
import androidx.ui.layout.ConstrainedBox
import androidx.ui.layout.DpConstraints
import androidx.ui.layout.Table
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.res.imageResource
import androidx.ui.tooling.preview.Preview
import kotlin.random.Random

private const val TAG = "mine"
private const val MINED = -99

@androidx.compose.Model
object Model {
    var row: Int = 10
    var column: Int = 6
    var mines: Int = 10
    val mineMap = SparseIntArray()
    var generated: Boolean = false

    fun generateMineMap() {
        generated = false
        mineMap.clear()
        val size = row * column
        /* calculate mines position */
        repeat(mines) {
            var i = Random.nextInt(0, size)
            while (mineMap.containsKey(i)) {
                i = Random.nextInt(0, size)
            }
            mineMap.put(i, MINED)
        }
        /* calculate mine count */
        repeat(size) { index ->
            if (isMine(index)) {
                Log.d(TAG, "mine @ $index")
            } else {//check 8-directions
                var count = 0
                if (!isFirstRow(index)) {
                    if (!isFirstColumn(index) && isMine(index - column - 1)) count++
                    if (isMine(index - column)) count++
                    if (!isLastColumn(index) && isMine(index - column + 1)) count++
                }
                if (!isFirstColumn(index) && isMine(index - 1)) count++
                if (!isLastColumn(index) && isMine(index + 1)) count++
                if (!isLastRow(index)) {
                    if (!isFirstColumn(index) && isMine(index + column - 1)) count++
                    if (isMine(index + column)) count++
                    if (!isLastColumn(index) && isMine(index + column + 1)) count++
                }
                mineMap.put(index, count)
            }
        }
        generated = true
    }

    fun toKey(row: Int, column: Int) = row * this.column + column
    private fun isMine(index: Int): Boolean = mineMap[index] == MINED
    private fun toRow(index: Int): Int = index / this.column
    private fun toColumn(index: Int): Int = index % this.column
    private fun isFirstRow(index: Int): Boolean = toRow(index) == 0
    private fun isLastRow(index: Int): Boolean = toRow(index) == this.column - 1
    private fun isFirstColumn(index: Int): Boolean = toColumn(index) == 0
    private fun isLastColumn(index: Int): Boolean = toColumn(index) == this.row - 1
    fun isMine(row: Int, column: Int) = isMine(toKey(row, column))
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                //Greeting("Android")
                if (Model.generated) {
                    Table(columns = Model.column, alignment = {
                        Alignment.Center
                    }) {
                        repeat(Model.row) { row ->
                            tableRow {
                                repeat(Model.column) { column ->
                                    FloorButton(row = row, column = column)
                                }
                            }
                        }
                    }
                } else {
                    Center {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        Model.generateMineMap()
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme {
        Greeting("Android")
    }
}

enum class State {
    None, Mined, Text, Marked
}

@Composable
fun FloorButton(row: Int = 0, column: Int = 0) {
    val clicked = +state { State.None }
    val constraints = DpConstraints.tightConstraints(height = 48.dp, width = 48.dp)
    when (clicked.value) {
        State.Marked ->
            Ripple(bounded = true) {
                LongPressGestureDetector(onLongPress = {
                    clicked.value = State.None
                }) {
                    ConstrainedBox(constraints = constraints) {
                        DrawImage(image = +imageResource(R.drawable.mine_marked))
                    }
                }
            }
        State.Mined ->
            ConstrainedBox(constraints = constraints) {
                DrawImage(image = +imageResource(R.drawable.mine_clicked))
            }
        State.Text ->
            ConstrainedBox(constraints = constraints) {
                Center {
                    Text(text = "${Model.mineMap[Model.toKey(row, column)]}")
                }
            }
        else ->
            Ripple(bounded = true) {
                LongPressGestureDetector(onLongPress = {
                    Log.d(TAG, "mark as mine")
                    clicked.value = State.Marked
                }) {
                    Clickable(onClick = {
                        Log.d(TAG, "click on $row $column")
                        if (Model.isMine(row, column)) {
                            clicked.value = State.Mined
                            Log.e(TAG, "gg!")
                        } else {
                            clicked.value = State.Text
                        }
                    }) {
                        ConstrainedBox(
                                constraints = DpConstraints(minHeight = 48.dp, minWidth = 48.dp)) {
                            DrawImage(image = +imageResource(R.drawable.box))
                        }
                    }
                }
            }
    }
}
