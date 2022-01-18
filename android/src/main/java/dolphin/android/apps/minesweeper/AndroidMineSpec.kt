package dolphin.android.apps.minesweeper

import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dolphin.desktop.apps.minesweeper.MineSpec

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
    strings: DefaultString = DefaultString(),
) : MineSpec(
    maxRows = rows, maxColumns = cols, maxMines = mines,
    facePainter = AndroidFacePainter(), blockPainter = AndroidBlockPainter(), strings = strings,
) {
    companion object {
        private const val TAG = "AndroidMineSpec"

        /**
         * Calculate max rows/columns for current screen size
         *
         * @return a pair of maximum (row, column)
         */
        private fun calculateScreenSize(displayMetrics: DisplayMetrics): Pair<Int, Int> {
            val height: Float = displayMetrics.heightPixels / displayMetrics.density
            val width: Float = displayMetrics.widthPixels / displayMetrics.density
            val r = kotlin.math.floor((height - 160) / BLOCK_SIZE).toInt()
            val c = kotlin.math.floor((width - 24) / BLOCK_SIZE).toInt()
            Log.v(TAG, "screen: $width x $height ==> rows = $r, columns = $c")
            return Pair(r, c)
        }
    }

    private constructor(size: Pair<Int, Int>, strings: DefaultString) :
            this(rows = size.first, cols = size.second, strings = strings)

    /**
     * Make a [MineSpec] for Android platform
     */
    constructor(displayMetrics: DisplayMetrics, context: Context) :
            this(calculateScreenSize(displayMetrics), AndroidDefaultString(context))
}

private class AndroidFacePainter(
    @DrawableRes happy: Int = R.drawable.face_smile,
    @DrawableRes joy: Int = R.drawable.face_win,
    @DrawableRes sad: Int = R.drawable.face_cry,
) : MineSpec.FacePainter(happy, joy, sad) {
    @Composable
    override fun painter(resource: Any): Painter = painterResource(resource as Int)
}

private class AndroidBlockPainter(
    @DrawableRes plain: Int = R.drawable.box,
    @DrawableRes mined: Int = R.drawable.mine_noclick,
    @DrawableRes marked: Int = R.drawable.mine_marked,
    @DrawableRes dead: Int = R.drawable.mine_clicked,
) : MineSpec.BlockPainter(plain, mined, marked, dead) {
    @Composable
    override fun painter(resource: Any): Painter = painterResource(resource as Int)
}

private class AndroidDefaultString(context: Context) : MineSpec.DefaultString(
    actionApply = context.getString(R.string.action_apply),
    actionShow = context.getString(R.string.action_config),
    actionHide = context.getString(R.string.action_hide),
    configRows = context.getString(R.string.config_row),
    configColumns = context.getString(R.string.config_column),
    configMines = context.getString(R.string.config_mine),
)
