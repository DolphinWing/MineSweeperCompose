package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.compose.WeakReference
import androidx.compose.unaryPlus
import androidx.ui.core.*
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.shape.border.Border
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.imageResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview

private const val TAG = "mine"
private const val BLOCK_SIZE = 48

class MainActivity : AppCompatActivity() {
    private lateinit var handler: MyHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler = MyHandler(this)
        setContent {
            MaterialTheme {
                MainUi(onNewGameCreate = {
                    Log.d(TAG, "create new map @ ${System.currentTimeMillis()}")
                    handler.removeMessages(0) //remove old clock
                    handler.sendEmptyMessageDelayed(0, 1000)
                })
            }
        }
        Handler().post {
            Log.d(TAG, "create new map")
            MineModel.generateMineMap()
            handler.sendEmptyMessageDelayed(0, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(0)
    }

    private class MyHandler(a: MainActivity) : Handler() {
        private val activity = WeakReference(a)

        override fun handleMessage(msg: Message) {
            activity.get()?.handleMessage(msg)
        }
    }

    private fun handleMessage(@Suppress("UNUSED_PARAMETER") msg: Message) {
        when {
            MineModel.state == MineModel.GameState.Start -> {
                //Log.d(TAG, "wait user click ${System.currentTimeMillis()}")
                handler.sendEmptyMessageDelayed(0, 1000)
            }
            MineModel.running -> {
                MineModel.clock++
                handler.sendEmptyMessageDelayed(0, 1000)
            }
            else -> {
                Log.v(TAG, "clock stopped!")
            }
        }
    }
}

@Composable
fun MainUi(onNewGameCreate: (() -> Unit)? = null) {
    if (MineModel.loading) {
        Column(crossAxisAlignment = CrossAxisAlignment.Center) {
            HeightSpacer(height = 32.dp)
            FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {
                expanded(1f) { MineCountWidget() }
                inflexible {
                    Ripple(bounded = true) {
                        Clickable(onClick = {
                            MineModel.generateMineMap()
                            if (onNewGameCreate != null) onNewGameCreate()
                        }) {
                            SmileyIcon(MineModel.state)
                        }
                    }
                }
                expanded(1f) { ClockWidget() }
            }
            HeightSpacer(height = 32.dp)
            MineField(row = MineModel.row, column = MineModel.column)
        }
    } else {
        Center {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun MineCountWidget() {
    Center {
        Text(
            String.format("%03d", MineModel.mines - MineModel.markedMines),
            style = TextStyle(
                color = Color.Red,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

@Composable
fun SmileyIcon(state: MineModel.GameState) {
    ConstrainedBox(constraints = DpConstraints.tightConstraints(height = 64.dp, width = 64.dp)) {
        DrawImage(
            image = +imageResource(
                when (state) {
                    MineModel.GameState.Exploded, MineModel.GameState.Review -> R.drawable.face_cry
                    MineModel.GameState.Cleared -> R.drawable.face_win
                    else -> R.drawable.face_smile
                }
            )
        )
    }
}

@Composable
fun ClockWidget() {
    ConstrainedBox(constraints = DpConstraints.tightConstraintsForWidth(64.dp)) {
        Center {
            Text(
                String.format("%05d", MineModel.clock),
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
        }
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
fun MineField(row: Int, column: Int) {
    Table(columns = column, columnWidth = { TableColumnWidth.Fixed(BLOCK_SIZE.dp) },
        alignment = { Alignment.Center }) {
        repeat(row) { row ->
            tableRow {
                repeat(column) { column ->
                    ConstrainedBox(
                        constraints = DpConstraints.tightConstraints(
                            height = BLOCK_SIZE.dp,
                            width = BLOCK_SIZE.dp
                        )
                    ) {
                        BlockButton(row = row, column = column)
                    }
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
fun BlockButton(row: Int, column: Int) {
    when (MineModel.blockMap[MineModel.toIndex(row, column)]) {
        MineModel.BlockState.Marked ->
            MarkedBlock(row, column)
        MineModel.BlockState.Mined ->
            MineBlock(clicked = true)
        MineModel.BlockState.Hidden ->
            MineBlock(clicked = false)
        MineModel.BlockState.Text ->
            TextBlock(row, column, debug = false)
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
                            constraints = DpConstraints.tightConstraints(
                                height = BLOCK_SIZE.dp,
                                width = BLOCK_SIZE.dp
                            )
                        ) {
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
            ConstrainedBox(
                constraints = DpConstraints.tightConstraints(
                    height = BLOCK_SIZE.dp,
                    width = BLOCK_SIZE.dp
                )
            ) {
                DrawImage(image = +imageResource(R.drawable.mine_marked))
            }
        }
    }
}

@Composable
fun MineBlock(clicked: Boolean = false) {
    Surface(border = Border(Color.White, 1.dp), color = Color.LightGray) {
        ConstrainedBox(
            constraints = DpConstraints.tightConstraints(
                height = BLOCK_SIZE.dp,
                width = BLOCK_SIZE.dp
            )
        ) {
            DrawImage(
                image = +imageResource(
                    if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick
                )
            )
        }
    }
}

@Composable
fun TextBlock(row: Int, column: Int, debug: Boolean = false) {
    val value = MineModel.mineMap[MineModel.toIndex(row, column)]
    if (debug) {
        //TextBlock(value = value, defaultColor = Color.Gray)
        ConstrainedBox(
            constraints = DpConstraints.tightConstraints(
                height = BLOCK_SIZE.dp,
                width = BLOCK_SIZE.dp
            )
        ) {
            Center { Text("$value", style = TextStyle(color = Color.Gray)) }
        }
    } else {
        TextBlock(value = value)
    }
}

private val textBlockColors = arrayOf(
    Color.White, Color.Blue, Color.Green.copy(green = .5f),
    Color.Red, Color.Blue.copy(blue = .4f), Color.Red.copy(red = .4f), Color.Magenta
)

@Composable
private fun TextBlock(value: Int) {
    Surface(border = Border(Color.White, 1.dp), color = Color.LightGray) {
        ConstrainedBox(
            constraints = DpConstraints.tightConstraints(
                height = BLOCK_SIZE.dp,
                width = BLOCK_SIZE.dp
            )
        ) {
            Center {
                Text(
                    text = "$value", style = TextStyle(
                        color = when {
                            value in 1..6 -> textBlockColors[value]
                            value > 6 -> textBlockColors.last()
                            value < 0 -> textBlockColors.first()
                            else -> Color.Black
                        }, fontWeight = if (value > 0) FontWeight.Bold else FontWeight.Normal
                    )
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewBlocks() {
    MaterialTheme {
        Column {
            Row {
                MarkedBlock(row = 0, column = 0)
                MineBlock(clicked = false)
                MineBlock(clicked = true)
                TextBlock(row = 0, column = 0, debug = false)
                TextBlock(row = 0, column = 0, debug = true)
            }
            Row {
                TextBlock(value = 0)
                TextBlock(value = 1)
                TextBlock(value = 2)
                TextBlock(value = 3)
                TextBlock(value = 4)
            }
            Row {
                TextBlock(value = 5)
                TextBlock(value = 6)
                TextBlock(value = 7)
                TextBlock(value = 8)
                TextBlock(value = -99)
            }
        }
    }
}
