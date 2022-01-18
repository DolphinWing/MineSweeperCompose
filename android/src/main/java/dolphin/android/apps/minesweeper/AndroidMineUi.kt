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
import dolphin.desktop.apps.minesweeper.BasicMineModel
import dolphin.desktop.apps.minesweeper.BlockButton
import dolphin.desktop.apps.minesweeper.BlockState
import dolphin.desktop.apps.minesweeper.ContentViewWidget
import dolphin.desktop.apps.minesweeper.MineSpec
import dolphin.desktop.apps.minesweeper.TextBlock

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
        model.mines.emit(10) // don't set too many mines first
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
            model = model,
            spec = spec,
            onVibrate = onVibrate,
            onNewGameCreated = onNewGameCreated,
        )
    }
}
