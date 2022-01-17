package dolphin.desktop.apps.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

open class MineSpec(
    val maxRows: Int = 12,
    val maxColumns: Int = 8,
    val maxMines: Int = 10,
    val face: FacePainter = FacePainter(),
    val block: BlockPainter = BlockPainter(),
    val strings: ConfigStrings = ConfigStrings(),
) {
    companion object {
        const val BLOCK_SIZE = 48
        const val SMILEY_SIZE = 64
    }

    private val textBlockColors = arrayOf(
        Color.White, Color.Blue, Color.Green.copy(green = .5f),
        Color.Red, Color.Blue.copy(blue = .4f), Color.Red.copy(red = .4f), Color.Magenta
    )

    fun textBlockColor(value: Int) = when {
        value in 1..6 -> textBlockColors[value]
        value > 6 -> textBlockColors.last()
        value < 0 -> textBlockColors.first()
        else -> Color.Black
    }

    open class FacePainter {
        @Composable
        open fun happy(): Painter = rememberVectorPainter(Icons.Default.Star)

        @Composable
        open fun sad(): Painter = rememberVectorPainter(Icons.Default.Star)

        @Composable
        open fun joy(): Painter = rememberVectorPainter(Icons.Default.Star)
    }

    open class BlockPainter {
        @Composable
        open fun plain(): Painter = rememberVectorPainter(Icons.Default.Star)

        @Composable
        open fun mined(): Painter = rememberVectorPainter(Icons.Default.Star)

        @Composable
        open fun dead(): Painter = rememberVectorPainter(Icons.Default.Star)

        @Composable
        open fun marked(): Painter = rememberVectorPainter(Icons.Default.Star)
    }

    open class ConfigStrings {
        open fun rows(): String = "Rows"
        open fun columns(): String = "Columns"
        open fun mines(): String = "Mines"
        open fun hide(): String = "Hide"
        open fun show(): String = "Show"
        open fun apply(): String = "Apply"
    }
}
