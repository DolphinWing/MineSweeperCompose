package dolphin.android.apps.minesweeper

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.Text
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.longPressGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageAsset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview

object MineUi {
    private const val TAG = "MineUi"
    private const val BLOCK_SIZE = 48
    private const val SMILEY_SIZE = 64

    fun calculateScreenSize(displayMetrics: DisplayMetrics): Pair<Int, Int> {
        val height: Float = displayMetrics.heightPixels / displayMetrics.density
        val width: Float = displayMetrics.widthPixels / displayMetrics.density
        val r = kotlin.math.floor((height - 220) / BLOCK_SIZE).toInt()
        val c = kotlin.math.floor((width - 24) / BLOCK_SIZE).toInt()
        Log.v(TAG, "screen: $width x $height ==> rows = $r, columns = $c")
        return Pair(r, c)
    }

    @Composable
    private fun imageDrawable(
        image: ImageAsset,
        width: Dp = BLOCK_SIZE.dp,
        height: Dp = BLOCK_SIZE.dp,
    ) {
        Image(image, modifier = Modifier.size(width, height), contentScale = ContentScale.Fit)
    }

    @Composable
    fun mainUi(
        rows: Int,
        column: Int,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        val model = remember { MineModel(rows, column) }

        contentViewWidget(
            maxRows = model.maxRows,
            maxCols = model.maxCols,
            maxMines = 40,
            row = model.row.value,
            column = model.column.value,
            mines = model.mines.value,
            loading = model.loading.value,
            onNewGameCreated = onNewGameCreated,
            model = model,
        )
    }

    @Composable
    fun contentViewWidget(
        maxRows: Int = 12,
        maxCols: Int = 8,
        maxMines: Int = 40,
        showConfig: Boolean = false,
        row: Int = 6,
        column: Int = 6,
        mines: Int = 10,
        loading: Boolean = false,
        model: MineModel? = null,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        MaterialTheme(
            colors = lightColors(
                primary = colorResource(R.color.colorPrimary),
                primaryVariant = colorResource(R.color.colorPrimaryDark),
                secondary = colorResource(R.color.colorAccent),
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                headerWidget(model = model, onNewGameCreated = onNewGameCreated)
                Box(modifier = Modifier.fillMaxHeight()) {
                    Box(
                        Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 24.dp),
                        alignment = Alignment.TopCenter,
                    ) {
                        if (loading) {
                            CircularProgressIndicator()
                        } else {
                            mineField(model = model, row = row, column = column)
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        configPane(
                            model = model,
                            maxRows = maxRows,
                            maxCols = maxCols,
                            maxMines = maxMines,
                            row = row,
                            column = column,
                            mine = mines,
                            showConfig = showConfig,
                            onNewGameCreated = onNewGameCreated,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun headerWidget(
        model: MineModel?,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        Row(Modifier.padding(32.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                mineCountWidget(model = model)
            }
            Box(modifier = Modifier.clickable(onClick = {
                model?.let { that ->
                    that.generateMineMap()
                    onNewGameCreated?.invoke(model)
                } // ?: kotlin.run { Log.e(TAG, "no model... HOW???") }
            })) {
                smileyIcon(model?.gameState?.value ?: MineModel.GameState.Start)
            }
            Box(Modifier.weight(1f)) {
                playClockWidget(model = model)
            }
        }
    }

    @Composable
    private fun mineCountWidget(model: MineModel?) {
        Box(modifier = Modifier.width(120.dp).padding(horizontal = 32.dp)) {
            Text(
                String.format("%03d", model?.remainingMines?.value ?: 0),
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                )
            )
        }
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
        imageDrawable(image, SMILEY_SIZE.dp, SMILEY_SIZE.dp)
    }

    @Composable
    private fun playClockWidget(model: MineModel?) {
        Box(modifier = Modifier.width(120.dp).padding(start = 16.dp, end = 8.dp)) {
            Text(
                String.format("%05d", model?.clock?.value ?: 0),
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                )
            )
        }
    }

    @Composable
    private fun mineField(model: MineModel?, row: Int, column: Int) {
        Column {
            repeat(row) { r ->
                Row {
                    repeat(column) { c ->
                        blockButton(
                            model = model,
                            row = r,
                            column = c,
                            blockState = model?.blockState?.get(model.toIndex(r, c))?.value
                                ?: MineModel.BlockState.None
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun blockButton(
        model: MineModel? = null,
        row: Int = 0,
        column: Int = 0,
        blockState: MineModel.BlockState = MineModel.BlockState.None,
        debug: Boolean? = null,
    ) {
        when (blockState) {
            MineModel.BlockState.Marked ->
                markedBlock(model, row, column)
            MineModel.BlockState.Mined ->
                mineBlock(clicked = true)
            MineModel.BlockState.Hidden ->
                mineBlock(clicked = false)
            MineModel.BlockState.Text ->
                textBlock(model, row, column, debug = debug ?: false)
            else ->
                baseBlock(
                    model = model,
                    row = row,
                    column = column,
                    debug = debug ?: model?.funny?.value ?: false,
                )
        }
    }

    @Composable
    private fun baseBlock(
        model: MineModel? = null,
        row: Int,
        column: Int,
        debug: Boolean = BuildConfig.DEBUG,
    ) {
        Box(modifier = Modifier.clickable(onClick = {
            if (model?.running == true) {
                model.stepOn(row, column)
            } else {
                Log.w(TAG, "current game state: ${model?.gameState?.value}")
            }
        }).longPressGestureFilter {
            if (model?.running == true) {
                if (model.markedMines <= model.mines.value) {
                    model.markAsMine(row, column)
                } else {
                    Log.e(TAG, "too many mines!!!")
                }
            } else {
                Log.w(TAG, "current game state: ${model?.gameState?.value}")
            }
        }.size(BLOCK_SIZE.dp, BLOCK_SIZE.dp)) {
            Box {
                Box { imageDrawable(image = imageResource(R.drawable.box)) }
                if (debug) Box { textBlock(model, row, column, debug = debug) }
            }
        }
    }

    @Composable
    private fun markedBlock(model: MineModel? = null, row: Int, column: Int) {
        Box(modifier = Modifier.size(BLOCK_SIZE.dp).longPressGestureFilter {
            if (model?.running == true) {
                model.changeState(row, column, MineModel.BlockState.None)
                --model.markedMines
            } else {
                Log.w(TAG, "not running")
            }
        }) {
            imageDrawable(image = imageResource(R.drawable.mine_marked))
        }
    }

    @Composable
    private fun mineBlock(clicked: Boolean = false) {
        val drawable = if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick
        Box(
            modifier = Modifier.size(BLOCK_SIZE.dp).border(BorderStroke(1.dp, color = Color.White))
                .background(Color.LightGray),
        ) {
            imageDrawable(image = imageResource(drawable))
        }
    }

    @Composable
    private fun textBlock(model: MineModel?, row: Int, column: Int, debug: Boolean = false) {
        // Log.d(TAG, "textBlock: $row $column $debug")
        val value = model?.getMineIndicator(row, column) ?: 0
        if (debug) {
            // TextBlock(value = value, defaultColor = Color.Gray)
            Box(modifier = Modifier.size(BLOCK_SIZE.dp), alignment = Alignment.Center) {
                Text(
                    if (value < 0) "*" else "$value",
                    style = TextStyle(color = Color.Gray),
                )
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
    internal fun textBlock(value: Int) {
        Box(
            modifier = Modifier.size(BLOCK_SIZE.dp).border(BorderStroke(1.dp, color = Color.White))
                .background(Color.LightGray),
            alignment = Alignment.Center,
        ) {
            Text(
                text = "$value",
                style = TextStyle(
                    color = getTextBlockColor(value),
                    fontWeight = if (value > 0) FontWeight.Bold else FontWeight.Normal,
                )
            )
        }
    }

    @Composable
    private fun configPane(
        model: MineModel?,
        maxRows: Int = 12,
        maxCols: Int = 8,
        maxMines: Int = 40,
        row: Int = 6,
        column: Int = 5,
        mine: Int = 10,
        showConfig: Boolean = false,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        val visible = remember { mutableStateOf(showConfig) }
        val rows = remember { mutableStateOf(row) }
        val columns = remember { mutableStateOf(column) }
        val mines = remember { mutableStateOf(mine) }

        val buttonText = stringResource(
            if (visible.value) R.string.action_hide else R.string.action_config
        )

        fun applyNewConfig() {
            model?.generateMineMap(rows.value, columns.value, mines.value)
            visible.value = model?.onTheWayToFunnyMode() == true
        }

        fun restoreConfig() {
            // reset values to current config
            rows.value = model?.row?.value ?: 6
            columns.value = model?.column?.value ?: 5
            mines.value = model?.mines?.value ?: 10
            // hide config pane
            visible.value = visible.value.not()
        }

        Surface(
            color = if (visible.value) Color.White else Color.Transparent,
            elevation = if (visible.value) 8.dp else 0.dp,
            // border = Border(Color.LightGray, 1.dp),
            shape = RoundedCornerShape(topRight = 16.dp, topLeft = 16.dp),
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column(verticalArrangement = Arrangement.SpaceAround) {
                    if (visible.value) {
                        textSlider(
                            title = stringResource(R.string.config_row),
                            initial = row,
                            start = 5,
                            end = maxRows,
                            onValueChanged = { rows.value = it },
                        )
                        textSlider(
                            title = stringResource(R.string.config_column),
                            initial = column,
                            start = 4,
                            end = maxCols,
                            onValueChanged = { columns.value = it },
                        )
                        textSlider(
                            title = stringResource(R.string.config_mine),
                            initial = mine,
                            start = 5,
                            end = maxMines,
                            step = 5,
                            onValueChanged = { mines.value = it },
                        )
                    } else {
                        Box(modifier = Modifier.height(1.dp)) {
                            // Text("...") // make the pane to match_parent width
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Row(horizontalArrangement = Arrangement.Start) {
                            TextButton(onClick = { restoreConfig() }) {
                                Text(buttonText)
                            }
                            if (visible.value) {
                                Button(onClick = {
                                    applyNewConfig()
                                    if (model != null) onNewGameCreated?.invoke(model)
                                }) {
                                    Text(stringResource(R.string.action_apply))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun textSlider(
        title: String = "",
        start: Int = 0,
        end: Int = 100,
        step: Int = 1,
        initial: Int = 0,
        onValueChanged: ((value: Int) -> Unit)? = null,
    ) {
        val position = remember { mutableStateOf(initial.toFloat()) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (title.isNotEmpty()) {
                Box(modifier = Modifier.width(64.dp)) {
                    Text(title, overflow = TextOverflow.Clip)
                }
            }
            Box(modifier = Modifier.width(24.dp)) {
                Text("$start", style = TextStyle(color = Color.Gray))
            }
            Box(modifier = Modifier.weight(1f)) {
                Slider(
                    value = position.value,
                    steps = (end - start - step) / step,
                    valueRange = start.toFloat()..end.toFloat(),
                    thumbColor = MaterialTheme.colors.primaryVariant,
                    onValueChange = { pos ->
                        // Log.d(TAG, "end value: $pos")
                        position.value = pos
                    },
                    onValueChangeEnd = {
                        if (onValueChanged != null) onValueChanged(position.value.toInt())
                    },
                )
            }
            Box(modifier = Modifier.width(36.dp)) {
                Text("$end", style = TextStyle(color = Color.Gray))
            }
        }
    }
}

@Preview(name = "Default layout", showDecoration = true)
@Composable
private fun defaultPreview() {
    MineUi.contentViewWidget(
        maxCols = 10,
        maxRows = 10,
        maxMines = 20,
        row = 6,
        column = 5,
        mines = 15,
        showConfig = true,
        loading = false,
    )
}

@Preview(name = "Mine block preview")
@Composable
private fun previewBlocks() {
    MaterialTheme {
        Column {
            Row {
                MineUi.blockButton(debug = true)
                MineUi.blockButton(debug = false)
                MineUi.blockButton(blockState = MineModel.BlockState.Marked)
                MineUi.blockButton(blockState = MineModel.BlockState.Mined)
                MineUi.blockButton(blockState = MineModel.BlockState.Hidden)
                MineUi.blockButton(blockState = MineModel.BlockState.Text, debug = true)
                MineUi.blockButton(blockState = MineModel.BlockState.Text, debug = false)
            }
            Row {
                repeat(8) { MineUi.textBlock(value = it) }
            }
        }
    }
}
