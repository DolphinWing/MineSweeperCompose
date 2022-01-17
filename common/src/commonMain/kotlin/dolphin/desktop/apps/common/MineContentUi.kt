@file:OptIn(ExperimentalFoundationApi::class)

package dolphin.desktop.apps.common

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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Real content of Main UI
 */
@Composable
fun ContentViewWidget(
    spec: MineSpec,
    showConfig: Boolean = false,
    row: Int = 6,
    column: Int = 6,
    mines: Int = 10,
    loading: Boolean = false,
    model: BasicMineModel? = null,
    onVibrate: (() -> Unit)? = null,
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
        ) {
            // Text("Game state: ${model?.gameState?.collectAsState()?.value}")
            HeaderWidget(model = model, onNewGameCreated = onNewGameCreated, spec = spec)
            MineField(
                model = model,
                spec = spec,
                row = row,
                column = column,
                onVibrate = onVibrate,
                modifier = Modifier.weight(1f),
            )
        }

        ConfigPane(
            modifier = Modifier.align(Alignment.BottomCenter),
            model = model,
            spec = spec,
            row = row,
            column = column,
            mine = mines,
            showConfig = showConfig,
            onNewGameCreated = onNewGameCreated,
        )

        if (loading) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = .5f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BlockImageDrawable(
    image: Painter,
    width: Dp = MineSpec.BLOCK_SIZE.dp,
    height: Dp = MineSpec.BLOCK_SIZE.dp,
) {
    Image(
        image,
        modifier = Modifier.size(width, height),
        contentScale = ContentScale.Fit,
        contentDescription = null,
    )
}

@Composable
private fun HeaderWidget(
    model: BasicMineModel?,
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
    spec: MineSpec,
) {
    val composableScope = rememberCoroutineScope()

    Row(
        modifier = Modifier.padding(24.dp),
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
                SmileyIcon(
                    mineModel.gameState.collectAsState().value,
                    cryDrawable = spec.face.sad(),
                    winDrawable = spec.face.joy(),
                    normalDrawable = spec.face.happy(),
                )
            }
        } ?: kotlin.run { Icon(Icons.Default.Refresh, contentDescription = null) }

        PlayClockWidget(model = model, Modifier.weight(1f))
    }
}

@Composable
private fun MineCountWidget(model: BasicMineModel?, modifier: Modifier = Modifier) {
    Text(
        // String.format("%03d", model?.remainingMines?.collectAsState()?.value ?: 0),
        model?.remainingMines?.collectAsState()?.value?.toString() ?: "0",
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
private fun SmileyIcon(
    state: GameState,
    cryDrawable: Painter,
    winDrawable: Painter,
    normalDrawable: Painter,
) {
    val image = when (state) {
        GameState.Exploded, GameState.Review -> cryDrawable
        GameState.Cleared -> winDrawable
        else -> normalDrawable
    }
    BlockImageDrawable(image, MineSpec.SMILEY_SIZE.dp, MineSpec.SMILEY_SIZE.dp)
}

@Composable
private fun PlayClockWidget(model: BasicMineModel?, modifier: Modifier = Modifier) {
    Text(
        // String.format("%05d", model?.clock?.collectAsState()?.value ?: 0,
        model?.clock?.collectAsState()?.value?.toString() ?: "0",
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
    model: BasicMineModel?,
    row: Int,
    column: Int,
    spec: MineSpec,
    modifier: Modifier = Modifier,
    onVibrate: (() -> Unit)? = null,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        repeat(row) { r ->
            Row {
                repeat(column) { c ->
                    if (model == null) {
                        BlockButton(BlockState.None, row = r, column = c, spec = spec)
                    } else {
                        BlockButtonImpl(model, r, c, onVibrate = onVibrate, spec = spec)
                    }
                }
            }
        }
    }
}

@Composable
private fun BlockButtonImpl(
    model: BasicMineModel,
    row: Int,
    column: Int,
    spec: MineSpec,
    onVibrate: (() -> Unit)? = null,
) {
    val state = model.gameState.collectAsState()
    val block = model.blockState[model.toIndex(row, column)]
    when (state.value) {
        GameState.Review, GameState.Exploded ->
            BlockButton(
                model = model,
                row = row,
                column = column,
                blockState = block.value,
                spec = spec,
            )
        else ->
            BlockButton(
                model = model,
                row = row,
                column = column,
                blockState = block.collectAsState().value,
                onVibrate = onVibrate,
                spec = spec,
            )
    }
}

/**
 * A block button in the mine field.
 */
@Composable
fun BlockButton(
    blockState: BlockState,
    model: BasicMineModel? = null,
    row: Int = 0,
    column: Int = 0,
    debug: Boolean? = null,
    onVibrate: (() -> Unit)? = null,
    spec: MineSpec = MineSpec(),
) {
    when (blockState) {
        BlockState.Marked ->
            MarkedBlock(model, row, column, onVibrate = onVibrate, spec = spec)
        BlockState.Mined, BlockState.Hidden ->
            MineBlock(clicked = blockState == BlockState.Mined, spec = spec)
        BlockState.Text ->
            TextBlock(model, row, column, debug = debug ?: false)
        else ->
            BaseBlock(
                model = model,
                row = row,
                column = column,
                debug = debug ?: model?.funny?.collectAsState()?.value ?: false,
                onVibrate = onVibrate,
                spec = spec,
            )
    }
}

@Composable
private fun BaseBlock(
    model: BasicMineModel? = null,
    row: Int,
    column: Int,
    debug: Boolean = false,
    onVibrate: (() -> Unit)? = null,
    spec: MineSpec = MineSpec(),
) {
    val composableScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(MineSpec.BLOCK_SIZE.dp)
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
            BlockImageDrawable(image = spec.block.plain())
            if (debug) Box { TextBlock(model, row, column, debug = debug) }
        }
    }
}

@Composable
private fun MarkedBlock(
    model: BasicMineModel? = null,
    row: Int,
    column: Int,
    onVibrate: (() -> Unit)? = null,
    spec: MineSpec = MineSpec(),
) {
    val composableScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(MineSpec.BLOCK_SIZE.dp)
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
        BlockImageDrawable(image = spec.block.marked())
    }
}

@Composable
private fun MineBlock(clicked: Boolean = false, spec: MineSpec = MineSpec()) {
    val drawable = if (clicked) spec.block.dead() else spec.block.mined()
    Box(
        modifier = Modifier
            .size(MineSpec.BLOCK_SIZE.dp)
            .border(BorderStroke(1.dp, color = Color.White))
            .background(Color.LightGray),
    ) {
        BlockImageDrawable(image = drawable)
    }
}

@Composable
private fun TextBlock(
    model: BasicMineModel?,
    row: Int,
    column: Int,
    spec: MineSpec = MineSpec(),
    debug: Boolean = false
) {
    // Log.d(TAG, "textBlock: $row $column $debug")
    val value = model?.getMineIndicator(row, column) ?: 0
    if (debug) {
        // TextBlock(value = value, defaultColor = Color.Gray)
        Box(modifier = Modifier.size(MineSpec.BLOCK_SIZE.dp), contentAlignment = Alignment.Center) {
            Text(
                if (value < 0) "*" else "$value",
                style = TextStyle(color = Color.Gray),
            )
        }
    } else {
        TextBlock(value = value, spec = spec)
    }
}

@Composable
fun TextBlock(value: Int, spec: MineSpec = MineSpec()) {
    Box(
        modifier = Modifier
            .size(MineSpec.BLOCK_SIZE.dp)
            .border(BorderStroke(1.dp, color = Color.White))
            .background(Color.LightGray),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$value",
            style = TextStyle(
                color = spec.textBlockColor(value),
                fontWeight = if (value > 0) FontWeight.Bold else FontWeight.Normal,
            )
        )
    }
}

@Composable
private fun ConfigPane(
    modifier: Modifier = Modifier,
    model: BasicMineModel? = null,
    spec: MineSpec = MineSpec(),
    row: Int = 6,
    column: Int = 5,
    mine: Int = 10,
    showConfig: Boolean = false,
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
    enableGodMode: Boolean = false,
) {
    val composeScope = rememberCoroutineScope()
    val visible = remember { mutableStateOf(showConfig) }
    val rows = remember { mutableStateOf(row) }
    val columns = remember { mutableStateOf(column) }
    val mines = remember { mutableStateOf(mine) }
    val composableScope = rememberCoroutineScope()

    val buttonText = if (visible.value) spec.strings.hide() else spec.strings.show()

    fun restoreConfig() {
        // reset values to current config
        rows.value = model?.rows?.value ?: 6
        columns.value = model?.columns?.value ?: 5
        mines.value = model?.mines?.value ?: 10
        // hide config pane
        visible.value = visible.value.not()
    }

    Surface(
        modifier = modifier,
        color = if (visible.value) Color.White else Color.Transparent,
        elevation = if (visible.value) 8.dp else 0.dp,
        // border = Border(Color.LightGray, 1.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(8.dp)
        ) {
            if (visible.value) {
                TextedSlider(
                    title = spec.strings.rows(),
                    initial = row,
                    start = 5,
                    end = spec.maxRows,
                    onValueChanged = { rows.value = it },
                )
                TextedSlider(
                    title = spec.strings.columns(),
                    initial = column,
                    start = 4,
                    end = spec.maxColumns,
                    onValueChanged = { columns.value = it },
                )
                TextedSlider(
                    title = spec.strings.mines(),
                    initial = mine,
                    start = 5,
                    end = spec.maxMines,
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
                    .padding(horizontal = 8.dp),
            ) {
                TextButton(onClick = { restoreConfig() }) {
                    Text(buttonText)
                }
                if (visible.value) {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
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
                        Text(spec.strings.apply())
                    }
                }
                if (enableGodMode) {
                    TextButton(onClick = { composeScope.launch { model?.funny?.emit(true) } }) {
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
