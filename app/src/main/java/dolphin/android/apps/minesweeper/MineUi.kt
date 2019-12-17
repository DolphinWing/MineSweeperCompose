package dolphin.android.apps.minesweeper

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
import androidx.ui.foundation.shape.corner.CircleShape
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.SolidColor
import androidx.ui.layout.*
import androidx.ui.material.*
import androidx.ui.material.ripple.Ripple
import androidx.ui.material.surface.Surface
import androidx.ui.res.colorResource
import androidx.ui.res.imageResource
import androidx.ui.text.TextStyle
import androidx.ui.text.font.FontFamily
import androidx.ui.text.font.FontWeight
import androidx.ui.tooling.preview.Preview

private const val TAG = "MineUi"
private const val BLOCK_SIZE = 48

@Preview
@Composable
fun DefaultPreview() {
    MaterialTheme(
            colors = ColorPalette(
                    primary = +colorResource(R.color.colorPrimary),
                    primaryVariant = +colorResource(R.color.colorPrimaryDark),
                    secondary = +colorResource(R.color.colorAccent)
            )
    ) {
        FlexColumn(crossAxisAlignment = CrossAxisAlignment.Center) {
            inflexible { HeaderWidget(onNewGameCreate = {}) }
            expanded(1f) {
                Stack {
                    expanded {
                        Container(expanded = true, alignment = Alignment.TopCenter,
                                padding = EdgeInsets(left = 8.dp, right = 8.dp, bottom = 24.dp)) {
                            MineField(row = 8, column = 5)
                        }
                    }
                    positioned(bottomInset = 8.dp, rightInset = 8.dp) {
                        ConfigPane()
                    }
                }
            }
        }
    }
}

@Composable
fun ContentViewWidget(onNewGameCreate: (() -> Unit)? = null) {
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
                        Container(expanded = true, alignment = Alignment.TopCenter,
                                padding = EdgeInsets(left = 8.dp, right = 8.dp, bottom = 24.dp)) {
                            if (MineModel.loading) {
                                CircularProgressIndicator()
                            } else {
                                MineField(row = MineModel.row, column = MineModel.column)
                            }
                        }
                    }
                    positioned(bottomInset = 0.dp, rightInset = 0.dp) {
                        ConfigPane()
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderWidget(onNewGameCreate: (() -> Unit)? = null) {
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
fun MineCountWidget() {
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

//@Preview
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

@Composable
fun ConfigPane() {
    val visible = +state { false }
    val rows = +state { MineModel.row }
    val columns = +state { MineModel.column }
    val mines = +state { MineModel.mines }

    Surface(color = if (visible.value) Color.White else Color.Transparent,
            elevation = if (visible.value) 8.dp else 0.dp,
            //border = Border(Color.LightGray, 1.dp),
            shape = RoundedCornerShape(topRight = 16.dp, topLeft = 16.dp)) {
        Padding(padding = 16.dp) {
            Column(arrangement = Arrangement.End) {
                if (visible.value) {
                    TextSlider(start = 5, end = 12, initial = rows.value, onValueChanged = {
                        rows.value = it
                    })
                    TextSlider(start = 4, end = 8, initial = columns.value, onValueChanged = {
                        columns.value = it
                    })
                    TextSlider(start = 5, end = 40, initial = mines.value, onValueChanged = {
                        mines.value = it
                    })
                }
                FlexRow(mainAxisAlignment = MainAxisAlignment.End) {
                    expanded(1f) { WidthSpacer(width = 16.dp) }
                    inflexible {
                        if (visible.value) {
                            Button(text = "Apply", style = TextButtonStyle(), onClick = {
                                MineModel.row = rows.value
                                MineModel.column = columns.value
                                MineModel.mines = mines.value
                                MineModel.generateMineMap()
                            })
                        }
                    }
                    inflexible { WidthSpacer(width = 16.dp) }
                    inflexible {
                        Button(text = if (visible.value) "Hide" else "Config",
                                style = TextButtonStyle(), onClick = {
                            visible.value = visible.value.not()
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun TextSlider(start: Int = 0, end: Int = 100, step: Int = 1, initial: Int = 0,
               onValueChanged: ((value: Int) -> Unit)? = null) {
    val position = +memo {
        SliderPosition(initial = initial.toFloat(), steps = (end - start - 1) / step,
                valueRange = start.toFloat()..end.toFloat())
    }

    FlexRow(crossAxisAlignment = CrossAxisAlignment.Center) {
        inflexible {
            Container(width = 36.dp) {
                Text("$start")
            }
        }
        expanded(1f) {
            Slider(position, onValueChangeEnd = {
                Log.d(TAG, "end value: ${position.value}")
                if (onValueChanged != null) onValueChanged(position.value.toInt())
            })
        }
        inflexible {
            Container(width = 36.dp) {
                Text("$end")
            }
        }
    }
}
