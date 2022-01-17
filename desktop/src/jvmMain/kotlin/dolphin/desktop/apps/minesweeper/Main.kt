package dolphin.desktop.apps.minesweeper

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File

fun main() = application {
    val workingDir: String = System.getProperty("user.dir")
    println("workingDir = $workingDir")

    val debug = File(workingDir, "build").exists() // has build dir
    println("debug = $debug")

    Window(onCloseRequest = ::exitApplication, resizable = false) {
        DesktopMineUi(debug = debug)
    }
}
