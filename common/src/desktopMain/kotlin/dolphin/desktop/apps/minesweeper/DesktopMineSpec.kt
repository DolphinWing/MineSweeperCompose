package dolphin.desktop.apps.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

class DesktopMineSpec : MineSpec(
    maxRows = 8,
    maxColumns = 15,
    maxMines = 40,
    facePainter = DesktopFacePainter(),
    blockPainter = DesktopBlockPainter(),
) {
}

private class DesktopFacePainter(
    happy: String = "face_smile.png",
    joy: String = "face_win.png",
    sad: String = "face_cry.png",
) : MineSpec.FacePainter(happy, joy, sad) {
    @Composable
    override fun painter(resource: Any): Painter = painterResource(resource as String)
}

private class DesktopBlockPainter(
    plain: String = "box.png",
    mined: String = "mine_noclick.png",
    marked: String = "mine_marked.png",
    dead: String = "mine_clicked.png",
) : MineSpec.BlockPainter(plain, mined, marked, dead) {
    @Composable
    override fun painter(resource: Any): Painter = painterResource(resource as String)
}
