package dolphin.android.apps.minesweeper

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import dolphin.desktop.apps.minesweeper.BasicMineModel

/**
 * data model for app
 *
 * @param maxRows max rows of the map, depending on screen size
 * @param maxCols max columns of the map, depending on screen size
 * @param maxMines max mines in the map
 */
class AndroidMineModel(maxRows: Int, maxCols: Int, maxMines: Int) :
    BasicMineModel(maxRows, maxCols, maxMines) {
    companion object {
        private const val TAG = "MineModel"
    }

    override fun log(message: String) {
        Log.v(TAG, message)
    }

    override fun startTicking() {
        clockHandler.tick()
    }

    override fun stopTicking() {
        clockHandler.removeMessages(0)
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
