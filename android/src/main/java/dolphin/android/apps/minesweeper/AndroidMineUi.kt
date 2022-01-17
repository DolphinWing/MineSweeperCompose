package dolphin.android.apps.minesweeper

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import dolphin.desktop.apps.common.BasicMineModel
import dolphin.desktop.apps.common.BlockButton
import dolphin.desktop.apps.common.BlockState
import dolphin.desktop.apps.common.ContentViewWidget
import dolphin.desktop.apps.common.MineSpec
import dolphin.desktop.apps.common.TextBlock

/**
 * Main UI implementations
 */
@ExperimentalFoundationApi
@Composable
fun AndroidMineUi(
    spec: MineSpec,
    onVibrate: (() -> Unit)? = null,
    onNewGameCreated: ((model: BasicMineModel) -> Unit)? = null,
) {
    val model = remember { AndroidMineModel(spec.maxRows, spec.maxColumns, spec.maxMines) }

    LaunchedEffect(Unit) {
        model.generateMineMap()
    }

    MaterialTheme(
        colors = lightColors(
            primary = colorResource(R.color.colorPrimary),
            primaryVariant = colorResource(R.color.colorPrimaryDark),
            secondary = colorResource(R.color.colorAccent),
        )
    ) {
        ContentViewWidget(
            spec = spec,
            row = model.rows.collectAsState().value,
            column = model.columns.collectAsState().value,
            mines = model.mines.collectAsState().value,
            loading = model.loading.collectAsState().value,
            model = model,
            onVibrate = onVibrate,
            onNewGameCreated = onNewGameCreated,
        )
    }
}

@ExperimentalFoundationApi
@Preview(name = "Default layout", showSystemUi = true)
@Composable
private fun PreviewMainUi() {
    ContentViewWidget(
        row = 6,
        column = 5,
        mines = 15,
        showConfig = true,
        loading = false,
        spec = AndroidMineSpec(rows = 10, cols = 10, mines = 20),
    )
}

@ExperimentalFoundationApi
@Preview(name = "Mine block preview")
@Composable
private fun PreviewBlocks() {
    val spec = AndroidMineSpec()
    MaterialTheme {
        Column {
            Row {
                BlockButton(BlockState.None, debug = true, spec = spec)
                BlockButton(BlockState.None, debug = false, spec = spec)
                BlockButton(BlockState.Marked, spec = spec)
                BlockButton(BlockState.Mined, spec = spec)
                BlockButton(BlockState.Hidden, spec = spec)
                BlockButton(BlockState.Text, debug = true, spec = spec)
                BlockButton(BlockState.Text, debug = false, spec = spec)
            }
            Row {
                repeat(8) { TextBlock(value = it) }
            }
        }
    }
}
