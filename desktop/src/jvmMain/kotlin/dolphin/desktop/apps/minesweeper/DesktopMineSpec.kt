package dolphin.desktop.apps.minesweeper

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import dolphin.desktop.apps.common.MineSpec

class DesktopMineSpec : MineSpec(
    maxRows = 8,
    maxColumns = 15,
    maxMines = 40,
    face = DesktopFacePainter(),
    block = DesktopBlockPainter(),
) {
}

private class DesktopFacePainter : MineSpec.FacePainter {
    @Composable
    override fun happy(): Painter = painterResource("face_smile.png")

    @Composable
    override fun sad(): Painter = painterResource("face_cry.png")

    @Composable
    override fun joy(): Painter = painterResource("face_win.png")
}

private class DesktopBlockPainter : MineSpec.BlockPainter {
    @Composable
    override fun plain(): Painter = painterResource("box.png")

    @Composable
    override fun marked(): Painter = painterResource("mine_marked.png")

    @Composable
    override fun dead(): Painter = painterResource("mine_clicked.png")

    @Composable
    override fun mined(): Painter = painterResource("mine_noclick.png")
}
