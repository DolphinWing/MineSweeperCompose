package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.ui.core.setContent

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MineActivity"
    }

    private lateinit var model: MineModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val maxSize = MineUi.calculateScreenSize(resources.displayMetrics)
        model = MineModel(maxRows = maxSize.first, maxCols = maxSize.second)

        setContent {
            MineUi.mainUi(model = model) {
                Log.d(TAG, "on new game created: ${model.row}x${model.column}")
                if (model.funny) toastAboutFunnyModeEnabled()
            }
        }

        /* delay everything start */
        Handler().post {
            model.funny = BuildConfig.DEBUG /* a funny mode for YA */
            model.generateMineMap()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.state = MineModel.GameState.Destroyed
    }

    private var toasted: Boolean = false

    private fun toastAboutFunnyModeEnabled() {
        if (!toasted) {
            runOnUiThread {
                Toast.makeText(this, R.string.toast_funny_mode_enabled, Toast.LENGTH_SHORT).show()
            }
            toasted = true
        }
    }
}
