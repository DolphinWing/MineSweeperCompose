package dolphin.android.apps.minesweeper

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.setContent

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MineActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val maxSize = MineUi.calculateScreenSize(resources.displayMetrics)

        setContent {
            MineUi.mainUi(maxSize.first, maxSize.second) { model ->
                Log.d(TAG, "on new game created: ${model.row}x${model.column}")
                if (model.funny.value) toastAboutFunnyModeEnabled()
            }
        }
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
