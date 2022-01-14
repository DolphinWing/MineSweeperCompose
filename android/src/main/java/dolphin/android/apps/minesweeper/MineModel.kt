package dolphin.android.apps.minesweeper

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import dolphin.desktop.apps.common.BaseMineModel

/**
 * data model for app
 *
 * @param maxRows max rows of the map, depending on screen size
 * @param maxCols max columns of the map, depending on screen size
 * @param maxMines max mines in the map
 */
class MineModel(maxRows: Int, maxCols: Int, maxMines: Int) :
    BaseMineModel(maxRows, maxCols, maxMines) {
    companion object {
        private const val TAG = "MineModel"
    }

    override fun log(message: String) {
        Log.v(TAG, message)
    }

    override fun startTicking() {
        clockHandler.tick()
    }

    private val clockHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (running) {
                clock.value++
                tick()
            }
        }

        fun tick() {
            sendEmptyMessageDelayed(0, 1000)
        }
    }
}
