package dolphin.desktop.apps.minesweeper

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun DesktopMineUi(debug: Boolean = false) {
    val scope = rememberCoroutineScope()
    val model = remember { DesktopMineModel(scope) }

    LaunchedEffect(Unit) {
        model.generateMineMap()
        // model.funny.emit(debug)
    }

    MaterialTheme {
        ContentViewWidget(
            model = model,
            spec = DesktopMineSpec(),
            row = model.rows.collectAsState().value,
            column = model.columns.collectAsState().value,
            mines = model.mines.collectAsState().value,
            loading = model.loading.collectAsState().value,
            debug = debug,
        )
    }
}

@Preview
@Composable
private fun PreviewDesktopMineUi() {
    ContentViewWidget(
        spec = DesktopMineSpec(),
        row = 6,
        column = 5,
        mines = 10,
        loading = false,
    )
}

@Preview
@Composable
private fun PreviewBlocks() {
    val spec = DesktopMineSpec()
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
