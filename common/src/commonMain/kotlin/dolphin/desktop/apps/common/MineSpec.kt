package dolphin.desktop.apps.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * Mine game spec
 *
 * @property face [Painter] implementations by platform
 * @property block [Painter] implementations by platform
 * @property maxRows max rows
 * @property maxColumns max columns
 * @property maxMines max mines
 * @property strings ui strings
 */
open class MineSpec(
    val maxRows: Int = 12,
    val maxColumns: Int = 8,
    val maxMines: Int = 10,
    val face: FacePainter = DummyFacePainter(),
    val block: BlockPainter = DummyBlockPainter(),
    val strings: ConfigStrings = ConfigStrings(),
) {
    /**
     * @property BLOCK_SIZE block size
     * @property SMILEY_SIZE smiley button size
     */
    companion object {
        const val BLOCK_SIZE = 48
        const val SMILEY_SIZE = 64
    }

    private val textBlockColors = arrayOf(
        Color.White, Color.Blue, Color.Green.copy(green = .5f),
        Color.Red, Color.Blue.copy(blue = .4f), Color.Red.copy(red = .4f), Color.Magenta
    )

    /**
     * Mine number color
     *
     * @param value mine count
     * @return text color
     */
    fun textBlockColor(value: Int) = when {
        value in 1..6 -> textBlockColors[value]
        value > 6 -> textBlockColors.last()
        value < 0 -> textBlockColors.first()
        else -> Color.Black
    }

    /**
     * Smiley [Painter] implementations by platform
     */
    interface FacePainter {
        @Composable
        fun happy(): Painter

        @Composable
        fun sad(): Painter

        @Composable
        fun joy(): Painter
    }

    /**
     * Block [Painter] implementations by platform
     */
    interface BlockPainter {
        @Composable
        fun plain(): Painter

        @Composable
        fun mined(): Painter

        @Composable
        fun dead(): Painter

        @Composable
        fun marked(): Painter
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

private class DummyFacePainter : MineSpec.FacePainter {
    @Composable
    override fun happy(): Painter = rememberVectorPainter(Icons.Default.Star)

    @Composable
    override fun sad(): Painter = rememberVectorPainter(Icons.Default.Star)

    @Composable
    override fun joy(): Painter = rememberVectorPainter(Icons.Default.Star)
}

private class DummyBlockPainter : MineSpec.BlockPainter {
    @Composable
    override fun plain(): Painter = rememberVectorPainter(Icons.Default.Star)

    @Composable
    override fun mined(): Painter = rememberVectorPainter(Icons.Default.Star)

    @Composable
    override fun dead(): Painter = rememberVectorPainter(Icons.Default.Star)

    @Composable
    override fun marked(): Painter = rememberVectorPainter(Icons.Default.Star)
}
