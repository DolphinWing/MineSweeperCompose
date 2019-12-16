package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.core.setContent
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.res.imageResource
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
    val gameState = +state { MineModel.GameState.Start }
    val dialog = +state { false }
    if (dialog.value) {
        AlertDialog(onCloseRequest = {}, confirmButton = {
            Button(text = "restart", onClick = {
                MineModel.generateMineMap()
                gameState.value = MineModel.GameState.Start //reset game state
                dialog.value = false
            })
        }, dismissButton = {
            Button(text = "okay", style = TextButtonStyle(), onClick = {
                gameState.value = MineModel.GameState.Start //reset game state
                dialog.value = false
            })
        }, text = {
            Text(if (gameState.value == MineModel.GameState.Cleared) "PASS" else "GG")
        })
    }

    if (MineModel.loading) {
        Column(crossAxisAlignment = CrossAxisAlignment.Center) {
            HeightSpacer(height = 32.dp)
            MineField(onStateChanged = {
                gameState.value = it
                dialog.value = true
            })
            HeightSpacer(height = 32.dp)
            Button(text = "restart", onClick = {
                Log.d(TAG, "restart?")
                dialog.value = true
            })
        }
    } else {
        Center {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun MineField(row: Int = MineModel.row, column: Int = MineModel.column,
              onStateChanged: (state: MineModel.GameState) -> Unit) {
    Table(columns = column, columnWidth = { TableColumnWidth.Fixed(BLOCK_SIZE.dp) },
            alignment = { Alignment.Center }) {
        repeat(row) { row ->
            tableRow {
                repeat(column) { column ->
                    BlockButton(row = row, column = column, onStateChanged = onStateChanged)
                }
            }
        }
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme { MineField(row = 5, column = 5, onStateChanged = {}) }
}

@Composable
fun BlockButton(row: Int = 0, column: Int = 0,
                onStateChanged: (state: MineModel.GameState) -> Unit) {
    val constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp, width = BLOCK_SIZE.dp)

    fun checkGameState(state: MineModel.GameState) {
        when (state) {
            MineModel.GameState.Cleared, MineModel.GameState.Exploded -> onStateChanged(state)
            else -> {
                // do nothing
            }
        }
    }

    when (MineModel.stateMap[MineModel.toIndex(row, column)]) {
        MineModel.BlockState.Marked ->
            MarkedBlock(row, column)
        MineModel.BlockState.Mined ->
            MineBlock()
        MineModel.BlockState.Text ->
            TextBlock(row, column)
        else ->
            Ripple(bounded = true) {
                LongPressGestureDetector(onLongPress = {
                    if (MineModel.markedMines <= MineModel.mines) {
                        checkGameState(MineModel.markAsMine(row, column))
                    } else {
                        Log.e(TAG, "too many mines!!!")
                    }
                }) {
                    Clickable(onClick = {
                        checkGameState(MineModel.stepOn(row, column))
                    }) {
                        ConstrainedBox(constraints = constraints) {
                            DrawImage(image = +imageResource(R.drawable.box))
                            TextBlock(row, column)
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
            MineModel.changeState(row, column, MineModel.BlockState.None)
        }) {
            ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
                    width = BLOCK_SIZE.dp)) {
                DrawImage(image = +imageResource(R.drawable.mine_marked))
            }
        }
    }
}

@Composable
fun MineBlock() {
    ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
            width = BLOCK_SIZE.dp)) {
        DrawImage(image = +imageResource(R.drawable.mine_clicked))
    }
}

@Composable
fun TextBlock(row: Int, column: Int) {
    ConstrainedBox(constraints = DpConstraints.tightConstraints(height = BLOCK_SIZE.dp,
            width = BLOCK_SIZE.dp)) {
        Center {
            Text(text = "${MineModel.mineMap[MineModel.toIndex(row, column)]}")
        }
    }
}

@Preview
@Composable
fun PreviewBlocks() {
    MaterialTheme {
        Row {
            MarkedBlock(row = 0, column = 0)
            MineBlock()
            TextBlock(row = 0, column = 0)
        }
    }
}
