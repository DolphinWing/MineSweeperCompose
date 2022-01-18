package dolphin.desktop.apps.minesweeper

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
            debug = debug,
        )
    }
}

@Preview
@Composable
private fun PreviewDesktopMineUi() {
    ContentViewWidget(spec = DesktopMineSpec())
}

@Preview
@Composable
private fun PreviewDesktopMineUiShowConfig() {
    ContentViewWidget(spec = DesktopMineSpec(), showConfig = true)
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
