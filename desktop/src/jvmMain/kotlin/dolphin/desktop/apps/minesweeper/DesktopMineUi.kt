package dolphin.desktop.apps.minesweeper

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import dolphin.desktop.apps.common.ContentViewWidget

@Composable
fun DesktopMineUi(debug: Boolean = false) {
    val scope = rememberCoroutineScope()
    val model = remember { DesktopMineModel(scope) }

    LaunchedEffect(Unit) {
        model.generateMineMap()
        model.funny.emit(debug)
    }

    MaterialTheme {
        ContentViewWidget(
            model = model,
            spec = DesktopMineSpec(),
            row = model.rows.collectAsState().value,
            column = model.columns.collectAsState().value,
            mines = model.mines.collectAsState().value,
            loading = model.loading.collectAsState().value,
        )
    }
}
