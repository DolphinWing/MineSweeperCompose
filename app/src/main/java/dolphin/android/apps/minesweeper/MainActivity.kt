package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.shape.border.Border
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.layout.*
import androidx.ui.material.Button
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.imageResource
import androidx.ui.text.TextStyle
import androidx.ui.tooling.preview.Preview

private const val TAG = "mine"
private const val BLOCK_SIZE = 48

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainUi()
            }
        }
        Handler().post { MineModel.generateMineMap() }
    }
}

@Composable
fun MainUi() {
    if (MineModel.loading) {
        Column(crossAxisAlignment = CrossAxisAlignment.Center) {
            HeightSpacer(height = 32.dp)
            Ripple(bounded = true) {
                Clickable(onClick = {
                    MineModel.generateMineMap()
                }) {
                    SmileyIcon(MineModel.state)
                }
            }
            HeightSpacer(height = 32.dp)
            MineField()
        }
    } else {
        Center {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun SmileyIcon(state: MineModel.GameState) {
    ConstrainedBox(constraints = DpConstraints.tightConstraints(height = 64.dp,
            width = 64.dp)) {
        DrawImage(image = +imageResource(when (state) {
            MineModel.GameState.Exploded, MineModel.GameState.Review -> R.drawable.face_cry
            MineModel.GameState.Cleared -> R.drawable.face_win
            else -> R.drawable.face_smile
        }))
    }
}

@Preview
@Composable
fun SmileyIconPreview() {
    MaterialTheme {
        Row {
            SmileyIcon(state = MineModel.GameState.Start)
            SmileyIcon(state = MineModel.GameState.Exploded)
            SmileyIcon(state = MineModel.GameState.Review)
            SmileyIcon(state = MineModel.GameState.Cleared)
        }
    }
}

@Composable
fun MineField(row: Int = MineModel.row, column: Int = MineModel.column) {
    Table(columns = column, columnWidth = { TableColumnWidth.Fixed(BLOCK_SIZE.dp) },
            alignment = { Alignment.Center }) {
        repeat(row) { row ->
            tableRow {
                repeat(column) { column ->
                    BlockButton(row = row, column = column)
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme { MineField(row = 5, column = 5) }
}

@Composable
fun BlockButton(row: Int = 0, column: Int = 0) {
    when (MineModel.stateMap[MineModel.toIndex(row, column)]) {
        MineModel.BlockState.Marked ->
            MarkedBlock(row, column)
        MineModel.BlockState.Mined ->
            MineBlock(clicked = true)
        MineModel.BlockState.Hidden ->
            MineBlock(clicked = false)
        MineModel.BlockState.Text ->
            TextBlock(row, column)
        else ->
            Ripple(bounded = true) {
                LongPressGestureDetector(onLongPress = {
                    if (MineModel.running) {
                        if (MineModel.markedMines <= MineModel.mines) {
                            MineModel.markAsMine(row, column)
                        } else {
                            Log.e(TAG, "too many mines!!!")
                        }
                    } else {
                        Log.w(TAG, "current game state: ${MineModel.state}")
                    }
                }) {
                    Clickable(onClick = {
                        if (MineModel.running) {
                            MineModel.stepOn(row, column)
                        } else {
                            Log.w(TAG, "current game state: ${MineModel.state}")
                        }
                    }) {
                        ConstrainedBox(
                                constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
                                        width = BLOCK_SIZE.dp)) {
                            DrawImage(image = +imageResource(R.drawable.box))
                            TextBlock(row, column, debug = BuildConfig.DEBUG)
                        }
                    }
                }
            }
    }
}

@Composable
fun MarkedBlock(row: Int, column: Int) {
    Ripple(bounded = true) {
        LongPressGestureDetector(onLongPress = {
            if (MineModel.running) {
                MineModel.changeState(row, column, MineModel.BlockState.None)
            } else {
                Log.w(TAG, "not running")
            }
        }) {
            ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
                    width = BLOCK_SIZE.dp)) {
                DrawImage(image = +imageResource(R.drawable.mine_marked))
            }
        }
    }
}

@Composable
fun MineBlock(clicked: Boolean = false) {
    ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
            width = BLOCK_SIZE.dp)) {
        DrawImage(image = +imageResource(
                if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick))
    }
}

@Composable
fun TextBlock(row: Int, column: Int, debug: Boolean = false) {
    val color = if (debug) Color.Transparent else Color.LightGray
    val borderColor = if (debug) Color.Transparent else Color.Gray
    Surface(border = Border(borderColor, 1.dp), color = color) {
        ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
                width = BLOCK_SIZE.dp)) {
            Center {
                Text(text = "${MineModel.mineMap[MineModel.toIndex(row, column)]}",
                        style = TextStyle(color = when {
                            debug -> Color.Gray
                            else -> Color.Black
                        }))
            }
        }
    }
}

@Preview
@Composable
fun PreviewBlocks() {
    MaterialTheme {
        Row {
            MarkedBlock(row = 0, column = 0)
            MineBlock(clicked = false)
            MineBlock(clicked = true)
            TextBlock(row = 0, column = 0)
            TextBlock(row = 0, column = 0, debug = true)
        }
    }
}
