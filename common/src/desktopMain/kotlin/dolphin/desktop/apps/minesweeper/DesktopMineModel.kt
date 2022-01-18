package dolphin.desktop.apps.minesweeper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DesktopMineModel(private val coroutineScope: CoroutineScope) : BasicMineModel() {
    override fun log(message: String) {
        println(message)
    }

    private var clockJob: Job? = null

    override fun startTicking() {
        clockJob = coroutineScope.launch {
            while (running) {
                delay(1000)
                clock.value++
            }
        }
    }

    override fun stopTicking() {
        clockJob?.cancel()
    }
}
