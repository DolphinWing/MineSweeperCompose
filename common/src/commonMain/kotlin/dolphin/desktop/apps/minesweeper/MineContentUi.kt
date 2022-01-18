package dolphin.desktop.apps.minesweeper

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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
    model: BasicMineModel = BasicMineModel(),
    spec: MineSpec = MineSpec(),
    showConfig: Boolean = false,
    onVibrate: (() -> Unit)? = null,
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
    debug: Boolean = false,
) {
    val rows = model.rows.collectAsState()
    val columns = model.columns.collectAsState()
    val mines = model.mines.collectAsState()
    val loading = model.loading.collectAsState()

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
                row = rows.value,
                column = columns.value,
                onVibrate = onVibrate,
                modifier = Modifier.weight(1f),
            )
        }

        ConfigPane(
            modifier = Modifier.align(Alignment.BottomCenter),
            model = model,
            spec = spec,
            row = rows.value,
            column = columns.value,
            mine = mines.value,
            showConfig = showConfig,
            onNewGameCreated = onNewGameCreated,
            enableGodMode = debug,
        )

        if (loading.value) {
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
private fun BlockImageDrawable(image: Painter, size: Dp = MineSpec.BLOCK_SIZE.dp) {
    Image(
        image,
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit,
        contentDescription = null,
    )
}

@Composable
private fun HeaderWidget(
    spec: MineSpec = MineSpec(),
    model: BasicMineModel = BasicMineModel(),
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
) {
    val composableScope = rememberCoroutineScope()

    fun makeNewMap() {
        composableScope.launch {
            model.generateMineMap()
            onNewGameCreated?.invoke(model)
        }
    }

    Row(
        modifier = Modifier.padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MineCountWidget(model = model, modifier = Modifier.weight(1f))

        Box(modifier = Modifier.clickable(onClick = { makeNewMap() })) {
            SmileyIcon(state = model.gameState.collectAsState().value, spec = spec)
        }

        PlayClockWidget(model = model, Modifier.weight(1f))
    }
}

private fun Int.toMineString(padding: Int = 5): String = toString().padStart(padding, '0')

@Composable
private fun MineCountWidget(modifier: Modifier = Modifier, model: BasicMineModel? = null) {
    Text(
        (model?.remainingMines?.collectAsState()?.value ?: 0).toMineString(),
        style = TextStyle(color = Color.Red, fontSize = 24.sp, fontFamily = FontFamily.Monospace),
        modifier = modifier,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SmileyIcon(state: GameState, spec: MineSpec = MineSpec()) {
    val image = when (state) {
        GameState.Exploded, GameState.Review -> spec.facePainter.sad()
        GameState.Cleared -> spec.facePainter.joy()
        else -> spec.facePainter.happy()
    }
    BlockImageDrawable(image, spec.smileySize().dp)
}

private fun Long.toMineString(padding: Int = 5): String = toString().padStart(padding, '0')

@Composable
private fun PlayClockWidget(model: BasicMineModel?, modifier: Modifier = Modifier) {
    Text(
        (model?.clock?.collectAsState()?.value ?: 0).toMineString(),
        style = TextStyle(color = Color.Red, fontSize = 24.sp, fontFamily = FontFamily.Monospace),
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
 * A block button in the minefield.
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
    val composableScope = rememberCoroutineScope()
    val value = model?.getMineIndicator(row, column) ?: 0

    when (blockState) {
        BlockState.Text ->
            TextBlock(value = value, debug = debug ?: false, spec = spec)

        BlockState.Mined, BlockState.Hidden ->
            MineBlock(clicked = blockState == BlockState.Mined, spec = spec)

        BlockState.Marked ->
            MarkedBlock(
                onLongClick = {
                    composableScope.launch {
                        model?.unmarkMine(row, column)
                    }
                    onVibrate?.invoke()
                },
                spec = spec,
            )

        else ->
            BasicBlock(
                value = value,
                debug = debug ?: model?.funny?.collectAsState()?.value ?: false,
                spec = spec,
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
            )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BasicBlock(
    value: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    spec: MineSpec = MineSpec(),
    debug: Boolean = false,
) {
    Box(
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        BlockImageDrawable(image = spec.blockPainter.plain(), size = spec.blockSize().dp)
        if (debug) Box { TextBlock(value, debug = debug, spec = spec) }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarkedBlock(onLongClick: () -> Unit, spec: MineSpec = MineSpec()) {
    Box(
        modifier = Modifier.combinedClickable(
            onClick = { /* only support long click here */ },
            onLongClick = onLongClick,
        ),
    ) {
        BlockImageDrawable(image = spec.blockPainter.marked(), size = spec.blockSize().dp)
    }
}

@Composable
private fun MineBlock(clicked: Boolean = false, spec: MineSpec = MineSpec()) {
    val drawable = if (clicked) spec.blockPainter.dead() else spec.blockPainter.mined()

    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, color = Color.White))
            .background(Color.LightGray),
    ) {
        BlockImageDrawable(image = drawable, size = spec.blockSize().dp)
    }
}

@Composable
fun TextBlock(value: Int, spec: MineSpec = MineSpec(), debug: Boolean = false) {
    if (debug) {
        // TextBlock(value = value, defaultColor = Color.Gray)
        Box(modifier = Modifier.size(spec.blockSize().dp), contentAlignment = Alignment.Center) {
            Text(
                if (value < 0) "*" else "$value",
                style = TextStyle(color = Color.Gray),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(spec.blockSize().dp)
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
}

@Composable
private fun ConfigPane(
    modifier: Modifier = Modifier,
    model: BasicMineModel = BasicMineModel(),
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
    val funny = model.funny.collectAsState()

    val buttonText = if (visible.value) spec.strings.actionHide else spec.strings.actionShow

    fun restoreConfig() {
        // reset values to current config
        rows.value = model.rows.value
        columns.value = model.columns.value
        mines.value = model.mines.value
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
                    title = spec.strings.configRows,
                    initial = row,
                    start = 5,
                    end = spec.maxRows,
                    onValueChanged = { rows.value = it },
                )
                TextedSlider(
                    title = spec.strings.configColumns,
                    initial = column,
                    start = 4,
                    end = spec.maxColumns,
                    onValueChanged = { columns.value = it },
                )
                TextedSlider(
                    title = spec.strings.configMines,
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            ) {
                TextButton(onClick = { restoreConfig() }) {
                    Text(buttonText)
                }
                if (visible.value) {
                    Spacer(modifier = Modifier.requiredWidth(8.dp))
                    Button(onClick = {
                        composeScope.launch {
                            model.generateMineMap(rows.value, columns.value, mines.value)
                            visible.value = model.onTheWayToFunnyMode() == true
                            onNewGameCreated?.invoke(model)
                        }
                    }) {
                        Text(spec.strings.actionApply)
                    }
                }
                if (enableGodMode) {
                    GodToggleButton(
                        onClick = {
                            composeScope.launch { model.funny.emit(!model.funny.value) }
                        },
                        checked = funny.value,
                    )
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
            onValueChange = { pos -> position.value = pos },
            onValueChangeFinished = { onValueChanged?.invoke(position.value.toInt()) },
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = MaterialTheme.colors.primaryVariant),
        )
        Text(
            "$end",
            modifier = Modifier.requiredWidth(36.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Composable
private fun GodToggleButton(onClick: () -> Unit, checked: Boolean = false) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (checked) MaterialTheme.colors.onSecondary else MaterialTheme.colors.primary,
            backgroundColor = if (checked) MaterialTheme.colors.secondary else Color.Transparent
        )
    ) {
        Text("GOD")
    }
}
