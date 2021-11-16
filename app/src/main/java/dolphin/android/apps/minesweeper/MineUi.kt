package dolphin.android.apps.minesweeper

import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Main UI implementations
 */
@ExperimentalFoundationApi
object MineUi {
    private const val TAG = "MineUi"
    private const val BLOCK_SIZE = 48
    private const val SMILEY_SIZE = 64

    /**
     * Calculate max rows/columns for current screen size
     *
     * @return a pair of maximum (row, column)
     */
    fun calculateScreenSize(displayMetrics: DisplayMetrics): Pair<Int, Int> {
        val height: Float = displayMetrics.heightPixels / displayMetrics.density
        val width: Float = displayMetrics.widthPixels / displayMetrics.density
        val r = kotlin.math.floor((height - 220) / BLOCK_SIZE).toInt()
        val c = kotlin.math.floor((width - 24) / BLOCK_SIZE).toInt()
        Log.v(TAG, "screen: $width x $height ==> rows = $r, columns = $c")
        return Pair(r, c)
    }

    @Composable
    private fun BlockImageDrawable(
        image: Painter,
        width: Dp = BLOCK_SIZE.dp,
        height: Dp = BLOCK_SIZE.dp,
    ) {
        Image(
            image,
            modifier = Modifier.size(width, height),
            contentScale = ContentScale.Fit,
            contentDescription = null,
        )
    }

    /**
     * Main UI
     */
    @Composable
    fun MainUi(
        rows: Int = 6,
        column: Int = 5,
        onVibrate: (() -> Unit)? = null,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        val model = remember { MineModel(rows, column) }

        LaunchedEffect(Unit) {
            model.generateMineMap()
        }

        ContentViewWidget(
            maxRows = model.maxRows,
            maxCols = model.maxCols,
            maxMines = 40,
            row = model.row.collectAsState().value,
            column = model.column.collectAsState().value,
            mines = model.mines.collectAsState().value,
            loading = model.loading.collectAsState().value,
            model = model,
            onVibrate = onVibrate,
            onNewGameCreated = onNewGameCreated,
        )
    }

    /**
     * Real content of Main UI
     */
    @Composable
    fun ContentViewWidget(
        maxRows: Int = 12,
        maxCols: Int = 8,
        maxMines: Int = 40,
        showConfig: Boolean = false,
        row: Int = 6,
        column: Int = 6,
        mines: Int = 10,
        loading: Boolean = false,
        model: MineModel? = null,
        onVibrate: (() -> Unit)? = null,
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
                // Text("Game state: ${model?.gameState?.collectAsState()?.value}")
                HeaderWidget(model = model, onNewGameCreated = onNewGameCreated)
                Box(modifier = Modifier.weight(1f)) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        if (loading) {
                            CircularProgressIndicator()
                        } else {
                            MineField(
                                model = model,
                                row = row,
                                column = column,
                                onVibrate = onVibrate,
                            )
                        }
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        ConfigPane(
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
    private fun HeaderWidget(
        model: MineModel?,
        onNewGameCreated: ((model: MineModel) -> Unit)? = null,
    ) {
        val composableScope = rememberCoroutineScope()

        Row(
            modifier = Modifier.padding(32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MineCountWidget(model = model, Modifier.weight(1f))

            model?.let { mineModel ->
                Box(
                    modifier = Modifier.clickable(onClick = {
                        composableScope.launch {
                            mineModel.generateMineMap()
                            onNewGameCreated?.invoke(mineModel)
                        }
                    }),
                ) {
                    SmileyIcon(mineModel.gameState.collectAsState().value)
                }
            } ?: kotlin.run { Icon(Icons.Default.Refresh, contentDescription = null) }

            PlayClockWidget(model = model, Modifier.weight(1f))
        }
    }

    @Composable
    private fun MineCountWidget(model: MineModel?, modifier: Modifier = Modifier) {
        Text(
            String.format("%03d", model?.remainingMines?.collectAsState()?.value ?: 0),
            style = TextStyle(
                color = Color.Red,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
            ),
            modifier = modifier,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    private fun SmileyIcon(state: MineModel.GameState) {
        val image = painterResource(
            when (state) {
                MineModel.GameState.Exploded, MineModel.GameState.Review -> R.drawable.face_cry
                MineModel.GameState.Cleared -> R.drawable.face_win
                else -> R.drawable.face_smile
            }
        )
        BlockImageDrawable(image, SMILEY_SIZE.dp, SMILEY_SIZE.dp)
    }

    @Composable
    private fun PlayClockWidget(model: MineModel?, modifier: Modifier = Modifier) {
        Text(
            String.format("%05d", model?.clock?.collectAsState()?.value ?: 0),
            style = TextStyle(
                color = Color.Red,
                fontSize = 24.sp,
                fontFamily = FontFamily.Monospace,
            ),
            modifier = modifier,
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    private fun MineField(
        model: MineModel?,
        row: Int,
        column: Int,
        onVibrate: (() -> Unit)? = null,
    ) {
        Column {
            repeat(row) { r ->
                Row {
                    repeat(column) { c ->
                        if (model == null) {
                            BlockButton(MineModel.BlockState.None, row = r, column = c)
                        } else {
                            BlockButtonImpl(model, r, c, onVibrate)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BlockButtonImpl(
        model: MineModel,
        row: Int,
        column: Int,
        onVibrate: (() -> Unit)? = null,
    ) {
        val state = model.gameState.collectAsState()
        val block = model.blockState[model.toIndex(row, column)]
        when (state.value) {
            MineModel.GameState.Review, MineModel.GameState.Exploded ->
                BlockButton(
                    model = model,
                    row = row,
                    column = column,
                    blockState = block.value,
                )
            else ->
                BlockButton(
                    model = model,
                    row = row,
                    column = column,
                    blockState = block.collectAsState().value,
                    onVibrate = onVibrate,
                )
        }
    }

    /**
     * A block button in the mine field.
     */
    @Composable
    fun BlockButton(
        blockState: MineModel.BlockState,
        model: MineModel? = null,
        row: Int = 0,
        column: Int = 0,
        debug: Boolean? = null,
        onVibrate: (() -> Unit)? = null,
    ) {
        when (blockState) {
            MineModel.BlockState.Marked ->
                MarkedBlock(model, row, column, onVibrate = onVibrate)
            MineModel.BlockState.Mined ->
                MineBlock(clicked = true)
            MineModel.BlockState.Hidden ->
                MineBlock(clicked = false)
            MineModel.BlockState.Text ->
                TextBlock(model, row, column, debug = debug ?: false)
            else ->
                BaseBlock(
                    model = model,
                    row = row,
                    column = column,
                    debug = debug ?: model?.funny?.observeAsState()?.value ?: false,
                    onVibrate = onVibrate,
                )
        }
    }

    @Composable
    private fun BaseBlock(
        model: MineModel? = null,
        row: Int,
        column: Int,
        debug: Boolean = BuildConfig.DEBUG,
        onVibrate: (() -> Unit)? = null,
    ) {
        val composableScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .size(BLOCK_SIZE.dp, BLOCK_SIZE.dp)
                .combinedClickable(
                    onClick = {
                        composableScope.launch {
                            model?.stepOnBlock(row, column)
                        }
                    },
                    onLongClick = {
                        composableScope.launch {
                            model?.markAsMineBlock(row, column)
                        }
                        onVibrate?.invoke()
                    },
                ),
        ) {
            Box {
                BlockImageDrawable(image = painterResource(R.drawable.box))
                if (debug) Box { TextBlock(model, row, column, debug = debug) }
            }
        }
    }

    @Composable
    private fun MarkedBlock(
        model: MineModel? = null,
        row: Int,
        column: Int,
        onVibrate: (() -> Unit)? = null,
    ) {
        val composableScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .size(BLOCK_SIZE.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = {
                        composableScope.launch {
                            model?.unmarkMine(row, column)
                        }
                        onVibrate?.invoke()
                    }
                ),
        ) {
            BlockImageDrawable(image = painterResource(R.drawable.mine_marked))
        }
    }

    @Composable
    private fun MineBlock(clicked: Boolean = false) {
        val drawable = if (clicked) R.drawable.mine_clicked else R.drawable.mine_noclick
        Box(
            modifier = Modifier
                .size(BLOCK_SIZE.dp)
                .border(BorderStroke(1.dp, color = Color.White))
                .background(Color.LightGray),
        ) {
            BlockImageDrawable(image = painterResource(drawable))
        }
    }

    @Composable
    private fun TextBlock(model: MineModel?, row: Int, column: Int, debug: Boolean = false) {
        // Log.d(TAG, "textBlock: $row $column $debug")
        val value = model?.getMineIndicator(row, column) ?: 0
        if (debug) {
            // TextBlock(value = value, defaultColor = Color.Gray)
            Box(modifier = Modifier.size(BLOCK_SIZE.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (value < 0) "*" else "$value",
                    style = TextStyle(color = Color.Gray),
                )
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
    fun TextBlock(value: Int) {
        Box(
            modifier = Modifier
                .size(BLOCK_SIZE.dp)
                .border(BorderStroke(1.dp, color = Color.White))
                .background(Color.LightGray),
            contentAlignment = Alignment.Center,
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
    private fun ConfigPane(
        model: MineModel? = null,
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
        val composableScope = rememberCoroutineScope()

        val buttonText = stringResource(
            if (visible.value) R.string.action_hide else R.string.action_config
        )

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
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(16.dp)
            ) {
                if (visible.value) {
                    TextedSlider(
                        title = stringResource(R.string.config_row),
                        initial = row,
                        start = 5,
                        end = maxRows,
                        onValueChanged = { rows.value = it },
                    )
                    TextedSlider(
                        title = stringResource(R.string.config_column),
                        initial = column,
                        start = 4,
                        end = maxCols,
                        onValueChanged = { columns.value = it },
                    )
                    TextedSlider(
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

                Row(
                    horizontalArrangement = Arrangement.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    TextButton(onClick = { restoreConfig() }) {
                        Text(buttonText)
                    }
                    if (visible.value) {
                        Button(onClick = {
                            composableScope.launch {
                                model?.generateMineMap(
                                    rows.value,
                                    columns.value,
                                    mines.value
                                )
                                visible.value = model?.onTheWayToFunnyMode() == true
                                if (model != null) onNewGameCreated?.invoke(model)
                            }
                        }) {
                            Text(stringResource(R.string.action_apply))
                        }
                    }
                    if (BuildConfig.DEBUG) {
                        TextButton(onClick = { model?.funny?.postValue(true) }) {
                            Text("GOD")
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TextedSlider(
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
                Text(
                    title,
                    modifier = Modifier.requiredWidth(64.dp),
                    style = MaterialTheme.typography.subtitle2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                )
            }
            Text(
                "$start",
                modifier = Modifier.requiredWidth(24.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
            )
            Slider(
                value = position.value,
                steps = (end - start - step) / step,
                valueRange = start.toFloat()..end.toFloat(),
                onValueChange = { pos ->
                    // Log.d(TAG, "end value: $pos")
                    position.value = pos
                },
                onValueChangeFinished = {
                    onValueChanged?.invoke(position.value.toInt())
                },
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colors.primaryVariant,
                ),
            )
            Text(
                "$end",
                modifier = Modifier.requiredWidth(36.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
            )
        }
    }
}

@ExperimentalFoundationApi
@Preview(name = "Default layout", showSystemUi = true)
@Composable
private fun PreviewMainUi() {
    MineUi.ContentViewWidget(
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

@ExperimentalFoundationApi
@Preview(name = "Mine block preview")
@Composable
private fun PreviewBlocks() {
    MaterialTheme {
        Column {
            Row {
                MineUi.BlockButton(MineModel.BlockState.None, debug = true)
                MineUi.BlockButton(MineModel.BlockState.None, debug = false)
                MineUi.BlockButton(MineModel.BlockState.Marked)
                MineUi.BlockButton(MineModel.BlockState.Mined)
                MineUi.BlockButton(MineModel.BlockState.Hidden)
                MineUi.BlockButton(MineModel.BlockState.Text, debug = true)
                MineUi.BlockButton(MineModel.BlockState.Text, debug = false)
            }
            Row {
                repeat(8) { MineUi.TextBlock(value = it) }
            }
        }
    }
}
