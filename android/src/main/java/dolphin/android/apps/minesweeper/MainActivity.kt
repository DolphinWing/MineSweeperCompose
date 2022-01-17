package dolphin.android.apps.minesweeper

import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.os.BuildCompat

@ExperimentalFoundationApi
class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MineActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val (maxRow, maxColumn) = AndroidMineSpec.calculateScreenSize(resources.displayMetrics)
        val spec = AndroidMineSpec(
            maxRow, maxColumn, mines = 10, strings = AndroidConfigStrings(this)
        )

        setContent {
            AndroidMineUi(
                spec = spec,
                onVibrate = { whenVibrate() },
                onNewGameCreated = { model ->
                    Log.d(TAG, "on new game created: ${model.rows}x${model.columns}")
                    if (model.funny.value) toastAboutFunnyModeEnabled()
                },
            )
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

    private fun whenVibrate() {
        when {
            BuildCompat.isAtLeastS() -> vibrateAtLeastS()
            BuildCompat.isAtLeastO() -> vibrateAtLeastO()
            else -> vibrateLegacy()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun vibrateAtLeastS() {
        val vibrator = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
        val effect = VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        vibrator.vibrate(CombinedVibration.createParallel(effect))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun vibrateAtLeastO() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        val effect = if (BuildCompat.isAtLeastQ()) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
        } else {
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        vibrator.vibrate(effect)
    }

    private fun vibrateLegacy() {
        (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(50)
    }
}
