package dolphin.android.apps.minesweeper

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.Composable
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Text
import androidx.ui.core.gesture.LongPressGestureDetector
import androidx.ui.core.toModifier
import androidx.ui.foundation.Border
import androidx.ui.foundation.Box
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.ContentGravity
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.Image
import androidx.ui.graphics.ScaleFit
import androidx.ui.graphics.painter.ImagePainter
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
import androidx.ui.text.style.TextOverflow
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import androidx.ui.unit.sp

private const val TAG = "MineUi"

class MineUi {
    companion object {
        internal const val BLOCK_SIZE = 48

        val blockConstraint = DpConstraints.fixed(BLOCK_SIZE.dp, BLOCK_SIZE.dp)
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

@Composable
fun contentViewWidget(maxRows: Int = 12, maxCols: Int = 8, maxMines: Int = 40,
                      showConfig: Boolean = false, row: Int = MineModel.row,
                      column: Int = MineModel.column, mines: Int = MineModel.mines,
                      onNewGameCreated: (() -> Unit)? = null
) {
    MaterialTheme(
            colors = lightColorPalette(
                    primary = colorResource(R.color.colorPrimary),
                    primaryVariant = colorResource(R.color.colorPrimaryDark),
                    secondary = colorResource(R.color.colorAccent)
            )
    ) {
        Column(arrangement = Arrangement.Center) {
            headerWidget(onNewGameCreated = onNewGameCreated)
            Stack(modifier = LayoutFlexible(1f)) {
                Box(LayoutSize.Fill, paddingLeft = 8.dp, paddingRight = 8.dp,
                        paddingBottom = 24.dp, gravity = ContentGravity.TopCenter) {
                    if (MineModel.loading) {
                        CircularProgressIndicator()
                    } else {
                        mineField(row = row, column = column)
                    }
                }
                Box(LayoutGravity.BottomCenter) {
                    configPane(
                            maxRows = maxRows, maxCols = maxCols, maxMines = maxMines,
                            row = row, column = column, mine = mines, showConfig = showConfig
                    )
                }
            }
        }
    }
}

@Composable
private fun headerWidget(onNewGameCreated: (() -> Unit)? = null) {
    Row(LayoutPadding(32.dp)) {
        Box(LayoutFlexible(1f), gravity = ContentGravity.Center) {
            mineCountWidget()
        }
        Box {
            Ripple(bounded = true) {
                Clickable(onClick = {
                    MineModel.generateMineMap()
                    if (onNewGameCreated != null) onNewGameCreated()
                }) {
                    smileyIcon(MineModel.state)
                }
            }
        }
        Box(LayoutFlexible(1f), gravity = ContentGravity.Center) {
            playClockWidget()
        }
    }
}

@Composable
private fun mineCountWidget() {
    Container(constraints = DpConstraints.fixedWidth(120.dp),
            padding = EdgeInsets(left = 32.dp, right = 32.dp)) {
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
private fun imageDrawable(image: Image, width: Dp = MineUi.BLOCK_SIZE.dp,
                          height: Dp = MineUi.BLOCK_SIZE.dp) {
    val imageModifier = ImagePainter(image).toModifier(scaleFit = ScaleFit.FillMaxDimension)
    Box(LayoutSize(width, height) + imageModifier)
}

@Composable
private fun smileyIcon(state: MineModel.GameState) {
    val image = imageResource(
            when (state) {
                MineModel.GameState.Exploded, MineModel.GameState.Review -> R.drawable.face_cry
                MineModel.GameState.Cleared -> R.drawable.face_win
                else -> R.drawable.face_smile
            }
    )
    imageDrawable(image, 64.dp, 64.dp)
}

@Composable
private fun playClockWidget() {
    Container(constraints = DpConstraints.fixedWidth(120.dp),
            padding = EdgeInsets(left = 16.dp, right = 8.dp)) {
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
private fun mineField(row: Int, column: Int) {
    Table(columns = column, columnWidth = { MineUi.columnConstraint },
            alignment = { Alignment.Center }) {
        repeat(row) { row ->
            tableRow {
                repeat(column) { column ->
                    Container(constraints = MineUi.blockConstraint) {
                        blockButton(row = row, column = column)
                    }
                }
            }
        }
    }
}

@Composable
private fun blockButton(row: Int, column: Int) {
    when (MineModel.blockState[MineModel.toIndex(row, column)]) {
        MineModel.BlockState.Marked ->
            markedBlock(row, column)
        MineModel.BlockState.Mined ->
            mineBlock(clicked = true)
        MineModel.BlockState.Hidden ->
            mineBlock(clicked = false)
        MineModel.BlockState.Text ->
            textBlock(row, column, debug = false)
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
                        baseBlock(row = row, column = column, debug = MineModel.funny)
                    }
                }
            }
    }
}

@Composable
private fun baseBlock(row: Int, column: Int, debug: Boolean = BuildConfig.DEBUG) {
    Container(constraints = MineUi.blockConstraint) {
        imageDrawable(image = imageResource(R.drawable.box))
        if (debug) textBlock(row, column, debug = debug)
    }
}

@Composable
private fun markedBlock(row: Int, column: Int) {
    Ripple(bounded = true) {
        LongPressGestureDetector(onLongPress = {
            if (MineModel.running) {
                MineModel.changeState(row, column, MineModel.BlockState.None)
            } else {
                Log.w(TAG, "not running")
            }
        }) {
            imageDrawable(image = imageResource(R.drawable.mine_marked))
        }
    }
}

@Composable
private fun mineBlock(clicked: Boolean = false) {
    val drawable = if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick
    Surface(border = Border(1.dp, color = Color.White), color = Color.LightGray) {
        imageDrawable(image = imageResource(drawable))
    }
}

@Composable
private fun textBlock(row: Int, column: Int, debug: Boolean = false) {
    val value = MineModel.getMineIndicator(row, column)
    if (debug) {
        //TextBlock(value = value, defaultColor = Color.Gray)
        Container(constraints = MineUi.blockConstraint) {
            Center { Text(if (value < 0) "*" else "$value", style = TextStyle(color = Color.Gray)) }
        }
    } else {
        textBlock(value = value)
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
private fun textBlock(value: Int) {
    Surface(border = Border(1.dp, color = Color.White), color = Color.LightGray) {
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

@Preview("Mine block preview")
@Composable
private fun previewBlocks() {
    MaterialTheme {
        Column {
            Row {
                baseBlock(row = 0, column = 0, debug = true)
                baseBlock(row = 0, column = 0, debug = false)
                markedBlock(row = 0, column = 0)
                mineBlock(clicked = false)
                mineBlock(clicked = true)
                //TextBlock(value = 7)
                //TextBlock(value = 8)
            }
            Row {
                repeat(7) { textBlock(value = it) }
                textBlock(value = -99)
            }
        }
    }
}

@Composable
private fun configPane(
        maxRows: Int = 12, maxCols: Int = 8, maxMines: Int = 40,
        row: Int = MineModel.row, column: Int = MineModel.column, mine: Int = MineModel.mines,
        showConfig: Boolean = false
) {
    val visible = state { showConfig }
    val rows = state { row }
    val columns = state { column }
    val mines = state { mine }

    val buttonText = stringResource(
            if (visible.value) R.string.action_hide else R.string.action_config
    )

    fun applyNewConfig() {
        MineModel.generateMineMap(rows.value, columns.value, mines.value)
        MineModel.funnyModeDetector()
        visible.value = false
    }

    fun restoreConfig() {
        //reset values to current config
        rows.value = MineModel.row
        columns.value = MineModel.column
        mines.value = MineModel.mines
        //hide config pane
        visible.value = visible.value.not()
    }

    Surface(
            color = if (visible.value) Color.White else Color.Transparent,
            elevation = if (visible.value) 8.dp else 0.dp,
            //border = Border(Color.LightGray, 1.dp),
            shape = RoundedCornerShape(topRight = 16.dp, topLeft = 16.dp)
    ) {
        Container(padding = EdgeInsets(16.dp)) {
            Column(arrangement = Arrangement.SpaceAround) {
                if (visible.value) {
                    textSlider(title = stringResource(R.string.config_row), initial = row,
                            start = 5, end = maxRows, onValueChanged = { rows.value = it })
                    textSlider(title = stringResource(R.string.config_column), initial = column,
                            start = 4, end = maxCols, onValueChanged = { columns.value = it })
                    textSlider(title = stringResource(R.string.config_mine), initial = mine,
                            start = 5, end = maxMines, step = 5,
                            onValueChanged = { mines.value = it })
                } else {
                    Container(constraints = DpConstraints.fixedHeight(1.dp), expanded = true) {
                        //Text("...") //make the pane to match_parent width
                    }
                }
                Box {
                    Row(arrangement = Arrangement.Start) {
                        TextButton(onClick = { restoreConfig() }) {
                            Text(buttonText)
                        }
                        if (visible.value) {
                            Container(padding = EdgeInsets(left = 16.dp, right = 16.dp)) {
                                Button(onClick = { applyNewConfig() }) {
                                    Text(stringResource(R.string.action_apply))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun textSlider(title: String = "", start: Int = 0, end: Int = 100, step: Int = 1,
                       initial: Int = 0, onValueChanged: ((value: Int) -> Unit)? = null
) {
    val position = SliderPosition(initial = initial.toFloat(), steps = (end - start - step) / step,
            valueRange = start.toFloat()..end.toFloat())
//    val pos = state { initial.toFloat() }

    Row {
        if (title.isNotEmpty()) {
            Box(gravity = ContentGravity.Center/*, backgroundColor = Color.Red*/) {
                Container(constraints = DpConstraints.fixedWidth(56.dp),
                        alignment = Alignment.Center) {
                    Text(title, overflow = TextOverflow.Clip)
                }
            }
        }
        Box(gravity = ContentGravity.Center/*, backgroundColor = Color.Yellow*/) {
            Container(constraints = DpConstraints.fixedWidth(36.dp)) {
                Text("$start", style = TextStyle(color = Color.Gray))
            }
        }
        Box(modifier = LayoutFlexible(1f)/*, backgroundColor = Color.Blue*/) {
            Slider(position, color = (MaterialTheme.colors()).secondary, onValueChangeEnd = {
                Log.d(TAG, "end value: ${position.value}")
                if (onValueChanged != null) onValueChanged(position.value.toInt())
            })
        }
        Box(gravity = ContentGravity.Center) {
            Container(constraints = DpConstraints.fixedWidth(36.dp)) {
                Text("$end", style = TextStyle(color = Color.Gray))
            }
        }
    }
}
