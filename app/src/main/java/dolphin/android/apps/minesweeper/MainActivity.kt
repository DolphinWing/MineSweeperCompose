package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.Composable
import androidx.ui.core.setContent
import androidx.ui.tooling.preview.Preview
import java.lang.ref.WeakReference

private const val TAG = "MineActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var handler: MyHandler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handler = MyHandler(this)

        val maxSize = MineUi.calculateScreenSize(resources.displayMetrics)

        setContent {
            contentViewWidget(
                    onNewGameCreated = {
                        handler.removeMessages(0) //remove old clock
                        handler.sendEmptyMessageDelayed(0, 1000)
                    },
                    maxRows = maxSize.first, maxCols = maxSize.second)
        }

        /* delay everything start */
        Handler().post {
            MineModel.funny = BuildConfig.DEBUG /* a funny mode for YA */
            MineModel.generateMineMap()
            handler.sendEmptyMessageDelayed(0, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeMessages(0)
    }

    private class MyHandler(a: MainActivity) : Handler() {
        private val activity = WeakReference(a)

        override fun handleMessage(msg: Message) {
            activity.get()?.handleMessage(msg)
        }
    }

    private fun handleMessage(@Suppress("UNUSED_PARAMETER") msg: Message) {
        when {
            MineModel.state == MineModel.GameState.Start -> {
                //Log.d(TAG, "wait user click ${System.currentTimeMillis()}")
                handler.sendEmptyMessageDelayed(0, 1000)
            }
            MineModel.running -> {
                MineModel.clock++
                handler.sendEmptyMessageDelayed(0, 1000)
            }
            else -> {
                Log.v(TAG, "clock stopped!")
            }
        }
    }
}

@Preview("Default layout")
@Composable
private fun defaultPreview() {
    contentViewWidget(
        maxCols = 10, maxRows = 10, maxMines = 20, row = 6, column = 5, mines = 15,
        showConfig = true
    )
}
