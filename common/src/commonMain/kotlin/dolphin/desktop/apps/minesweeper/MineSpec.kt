package dolphin.desktop.apps.minesweeper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter

/**
 * Mine game spec
 *
 * @property facePainter [Painter] implementations by platform
 * @property blockPainter [Painter] implementations by platform
 * @property maxRows max rows
 * @property maxColumns max columns
 * @property maxMines max mines
 * @property strings ui strings
 */
open class MineSpec(
    val maxRows: Int = 12,
    val maxColumns: Int = 8,
    val maxMines: Int = 10,
    val facePainter: FacePainter = DefaultFacePainter(),
    val blockPainter: BlockPainter = DefaultBlockPainter(),
    val strings: DefaultString = DefaultString(),
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
    abstract class FacePainter(private val happy: Any, private val joy: Any, private val sad: Any) {
        @Composable
        fun happy(): Painter = painter(happy)

        @Composable
        fun sad(): Painter = painter(sad)

        @Composable
        fun joy(): Painter = painter(joy)

        @Composable
        abstract fun painter(resource: Any): Painter
    }

    class DefaultFacePainter : FacePainter("happy", "joy", "sad") {
        @Composable
        override fun painter(resource: Any): Painter = rememberVectorPainter(Icons.Default.Star)
    }

    /**
     * Block [Painter] implementations by platform
     */
    abstract class BlockPainter(
        private val plain: Any,
        private val mined: Any,
        private val marked: Any,
        private val dead: Any,
    ) {
        @Composable
        fun plain(): Painter = painter(plain)

        @Composable
        fun mined(): Painter = painter(mined)

        @Composable
        fun dead(): Painter = painter(dead)

        @Composable
        fun marked(): Painter = painter(marked)

        @Composable
        abstract fun painter(resource: Any): Painter
    }

    class DefaultBlockPainter : BlockPainter("plain", "mined", "marked", "dead") {
        @Composable
        override fun painter(resource: Any): Painter = rememberVectorPainter(Icons.Default.Star)
    }

    open class DefaultString(
        val actionApply: String = "Apply",
        val actionHide: String = "Hide",
        val actionShow: String = "Show",
        val configRows: String = "Rows",
        val configColumns: String = "Columns",
        val configMines: String = "Mines",
    )
}
