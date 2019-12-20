package dolphin.android.apps.minesweeper

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.Composable
import androidx.compose.memo
import androidx.compose.state
import androidx.compose.unaryPlus
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.dp
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.core.sp
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.DrawImage
import androidx.ui.foundation.shape.border.Border
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.res.stringResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview

private const val TAG = "MineUi"

class MineUi {
    companion object {
        private const val BLOCK_SIZE = 48

        val blockConstraint = DpConstraints.tightConstraints(BLOCK_SIZE.dp, BLOCK_SIZE.dp)
        val columnConstraint = TableColumnWidth.Fixed(BLOCK_SIZE.dp)

        fun calculateScreenSize(displayMetrics: DisplayMetrics): Pair<Int, Int> {
            val height: Float = displayMetrics.heightPixels / displayMetrics.density
            val width: Float = displayMetrics.widthPixels / displayMetrics.density
            val r = kotlin.math.floor((height - 220) / BLOCK_SIZE).toInt()
            val c = kotlin.math.floor((width - 24) / BLOCK_SIZE).toInt()
            Log.v(TAG, "screen: $width x $height ==> rows = $r, columns = $c")
            return Pair(r, c)
        }
    }
}

@Preview
@Composable
private fun DefaultPreview() {
    ContentViewWidget(
            maxCols = 10, maxRows = 10, maxMines = 20, row = 6, column = 5, mines = 15,
            showConfig = true
    )
}

@Composable
fun ContentViewWidget(
        maxRows: Int = 12, maxCols: Int = 8, maxMines: Int = 40, showConfig: Boolean = false,
        row: Int = MineModel.row, column: Int = MineModel.column, mines: Int = MineModel.mines,
        onNewGameCreate: (() -> Unit)? = null
) {
    MaterialTheme(
            colors = ColorPalette(
                    primary = +colorResource(R.color.colorPrimary),
                    primaryVariant = +colorResource(R.color.colorPrimaryDark),
                    secondary = +colorResource(R.color.colorAccent)
            )
    ) {
        FlexColumn(crossAxisAlignment = CrossAxisAlignment.Center) {
            inflexible { HeaderWidget(onNewGameCreate = onNewGameCreate) }
            expanded(1f) {
                Stack {
                    expanded {
                        Container(
                                expanded = true, alignment = Alignment.TopCenter,
                                padding = EdgeInsets(left = 8.dp, right = 8.dp, bottom = 24.dp)
                        ) {
                            if (MineModel.loading) {
                                CircularProgressIndicator()
                            } else {
                                MineField(row = row, column = column)
                            }
                        }
                    }
                    positioned(bottomInset = 0.dp, rightInset = 0.dp) {
                        ConfigPane(
                                maxRows = maxRows, maxCols = maxCols, maxMines = maxMines,
                                row = row, column = column, mine = mines, showConfig = showConfig
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderWidget(onNewGameCreate: (() -> Unit)? = null) {
    Padding(padding = 32.dp) {
        FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {
            expanded(1f) {
                Padding(padding = EdgeInsets(left = 32.dp, right = 32.dp)) {
                    MineCountWidget()
                }
            }
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
            expanded(1f) {
                Padding(padding = EdgeInsets(left = 16.dp, right = 8.dp)) {
                    ClockWidget()
                }
            }
        }
    }
}

@Composable
private fun MineCountWidget() {
    ConstrainedBox(constraints = DpConstraints.tightConstraintsForWidth(120.dp)) {
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
private fun SmileyIcon(state: MineModel.GameState) {
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
private fun ClockWidget() {
    ConstrainedBox(constraints = DpConstraints.tightConstraintsForWidth(120.dp)) {
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

@Composable
private fun MineField(row: Int, column: Int) {
    Table(columns = column, columnWidth = { MineUi.columnConstraint },
            alignment = { Alignment.Center }) {
        repeat(row) { row ->
            tableRow {
                repeat(column) { column ->
                    ConstrainedBox(constraints = MineUi.blockConstraint) {
                        BlockButton(row = row, column = column)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockButton(row: Int, column: Int) {
    when (MineModel.blockState[MineModel.toIndex(row, column)]) {
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
                        Block(row = row, column = column)
                    }
                }
            }
    }
}

@Composable
private fun Block(row: Int, column: Int, debug: Boolean = BuildConfig.DEBUG) {
    ConstrainedBox(constraints = MineUi.blockConstraint) {
        DrawImage(image = +imageResource(R.drawable.box))
        if (debug) TextBlock(row, column, debug = debug)
    }
}

@Composable
private fun MarkedBlock(row: Int, column: Int) {
    Ripple(bounded = true) {
        LongPressGestureDetector(onLongPress = {
            if (MineModel.running) {
                MineModel.changeState(row, column, MineModel.BlockState.None)
            } else {
                Log.w(TAG, "not running")
            }
        }) {
            ConstrainedBox(constraints = MineUi.blockConstraint) {
                DrawImage(image = +imageResource(R.drawable.mine_marked))
            }
        }
    }
}

@Composable
private fun MineBlock(clicked: Boolean = false) {
    val drawable = if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick
    Surface(border = Border(Color.White, 1.dp), color = Color.LightGray) {
        ConstrainedBox(constraints = MineUi.blockConstraint) {
            DrawImage(image = +imageResource(drawable))
        }
    }
}

@Composable
private fun TextBlock(row: Int, column: Int, debug: Boolean = false) {
    val value = MineModel.getMineIndicator(row, column)
    if (debug) {
        //TextBlock(value = value, defaultColor = Color.Gray)
        ConstrainedBox(constraints = MineUi.blockConstraint) {
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

private fun getTextBlockColor(value: Int) = when {
    value in 1..6 -> textBlockColors[value]
    value > 6 -> textBlockColors.last()
    value < 0 -> textBlockColors.first()
    else -> Color.Black
}

@Composable
private fun TextBlock(value: Int) {
    Surface(border = Border(Color.White, 1.dp), color = Color.LightGray) {
        Container(constraints = MineUi.blockConstraint, alignment = Alignment.Center) {
            Text(
                    text = "$value", style = TextStyle(
                    color = getTextBlockColor(value),
                    fontWeight = if (value > 0) FontWeight.Bold else FontWeight.Normal
            )
            )
        }
    }
}

@Preview
@Composable
private fun PreviewBlocks() {
    MaterialTheme {
        Column {
            Row {
                Block(row = 0, column = 0, debug = true)
                Block(row = 0, column = 0, debug = false)
                MarkedBlock(row = 0, column = 0)
                MineBlock(clicked = false)
                MineBlock(clicked = true)
                //TextBlock(value = 7)
                //TextBlock(value = 8)
            }
            Row {
                repeat(7) { TextBlock(value = it) }
                TextBlock(value = -99)
            }
        }
    }
}

@Composable
private fun ConfigPane(
        maxRows: Int = 12, maxCols: Int = 8, maxMines: Int = 40,
        row: Int = MineModel.row, column: Int = MineModel.column, mine: Int = MineModel.mines,
        showConfig: Boolean = false
) {
    val visible = +state { showConfig }
    val rows = +state { row }
    val columns = +state { column }
    val mines = +state { mine }

    val buttonText = +stringResource(
            if (visible.value) R.string.action_hide else R.string.action_config)

    Surface(
            color = if (visible.value) Color.White else Color.Transparent,
            elevation = if (visible.value) 8.dp else 0.dp,
            //border = Border(Color.LightGray, 1.dp),
            shape = RoundedCornerShape(topRight = 16.dp, topLeft = 16.dp)
    ) {
        Padding(padding = 16.dp) {
            Column(arrangement = Arrangement.End) {
                if (visible.value) {
                    TextSlider(start = 5, end = maxRows, initial = rows.value,
                            onValueChanged = { rows.value = it })
                    TextSlider(start = 4, end = maxCols, initial = columns.value,
                            onValueChanged = { columns.value = it })
                    TextSlider(start = 5, end = maxMines, initial = mines.value, step = 5,
                            onValueChanged = { mines.value = it })
                }
                FlexRow(mainAxisAlignment = MainAxisAlignment.End) {
                    expanded(1f) { WidthSpacer(width = 16.dp) }
                    inflexible {
                        if (visible.value) {
                            Button(text = +stringResource(R.string.action_apply),
                                    style = TextButtonStyle(), onClick = {
                                MineModel.generateMineMap(rows.value, columns.value, mines.value)
                                visible.value = false
                            })
                        }
                    }
                    inflexible { WidthSpacer(width = 16.dp) }
                    inflexible {
                        Button(text = buttonText,
                                style = TextButtonStyle(), onClick = {
                            //reset values to current config
                            rows.value = MineModel.row
                            columns.value = MineModel.column
                            mines.value = MineModel.mines
                            //hide config pane
                            visible.value = visible.value.not()
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun TextSlider(
        start: Int = 0, end: Int = 100, step: Int = 1, initial: Int = 0,
        onValueChanged: ((value: Int) -> Unit)? = null
) {
    val position = +memo {
        SliderPosition(
                initial = initial.toFloat(), steps = (end - start - step) / step,
                valueRange = start.toFloat()..end.toFloat()
        )
    }

    FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {
        inflexible {
            Container(width = 36.dp) {
                Text("$start", style = TextStyle(color = Color.Gray))
            }
        }
        expanded(1f) {
            Slider(position, color = (+MaterialTheme.colors()).secondary, onValueChangeEnd = {
                Log.d(TAG, "end value: ${position.value}")
                if (onValueChanged != null) onValueChanged(position.value.toInt())
            })
        }
        inflexible {
            Container(width = 36.dp) {
                Text("$end", style = TextStyle(color = Color.Gray))
            }
        }
    }
}
