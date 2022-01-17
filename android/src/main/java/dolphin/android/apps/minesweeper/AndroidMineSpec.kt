package dolphin.android.apps.minesweeper

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dolphin.desktop.apps.common.MineSpec

/**
 * Android [MineSpec] implementation.
 *
 * @param rows max rows
 * @param cols max columns
 * @param mines max mines
 * @param strings string maps
 */
class AndroidMineSpec(
    rows: Int = 6,
    cols: Int = 5,
    mines: Int = 40,
    strings: ConfigStrings = ConfigStrings(),
) : MineSpec(
    maxRows = rows, maxColumns = cols, maxMines = mines,
    face = AndroidFacePainter(), block = AndroidBlockPainter(), strings = strings,
) {
    companion object {
        private const val TAG = "AndroidMineSpec"

        /**
         * Calculate max rows/columns for current screen size
         *
         * @return a pair of maximum (row, column)
         */
        fun calculateScreenSize(displayMetrics: DisplayMetrics): Pair<Int, Int> {
            val height: Float = displayMetrics.heightPixels / displayMetrics.density
            val width: Float = displayMetrics.widthPixels / displayMetrics.density
            val r = kotlin.math.floor((height - 160) / BLOCK_SIZE).toInt()
            val c = kotlin.math.floor((width - 24) / BLOCK_SIZE).toInt()
            Log.v(TAG, "screen: $width x $height ==> rows = $r, columns = $c")
            return Pair(r, c)
        }
    }
}

private class AndroidFacePainter : MineSpec.FacePainter {
    @Composable
    override fun sad(): Painter = painterResource(R.drawable.face_cry)

    @Composable
    override fun joy(): Painter = painterResource(R.drawable.face_win)

    @Composable
    override fun happy(): Painter = painterResource(R.drawable.face_smile)
}

private class AndroidBlockPainter : MineSpec.BlockPainter {
    @Composable
    override fun plain(): Painter = painterResource(R.drawable.box)

    @Composable
    override fun mined(): Painter = painterResource(R.drawable.mine_noclick)

    @Composable
    override fun marked(): Painter = painterResource(R.drawable.mine_marked)

    @Composable
    override fun dead(): Painter = painterResource(R.drawable.mine_clicked)
}

class AndroidConfigStrings(private val context: Context) : MineSpec.ConfigStrings() {
    override fun hide(): String = context.getString(R.string.action_hide)
    override fun show(): String = context.getString(R.string.action_config)
    override fun apply(): String = context.getString(R.string.action_apply)
    override fun rows(): String = context.getString(R.string.config_row)
    override fun columns(): String = context.getString(R.string.config_column)
    override fun mines(): String = context.getString(R.string.config_mine)
}
