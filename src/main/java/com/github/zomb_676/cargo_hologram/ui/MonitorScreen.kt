package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryRequestPack
import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryStopPack
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.QueryRequirement
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

class MonitorScreen : Screen("monitor".literal()) {

    private var cursor = AreaImmute.ofFullScreen().asBaseCursor()

    var mainArea: AreaImmute = cursor
        private set

    init {
        PlayerCenteredQueryRequestPack(
            currentClientPlayer().uuid,
            2,
            QueryRequirement(force = false, crossDimension = false)
        ).sendToServer()
    }

    override fun init() {
        cursor = AreaImmute.ofSize(width, height).asBaseCursor()
        mainArea = cursor.percentX(0.6).percentY(0.8).asAreaImmute()
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.fill(ARGBColor.Presets.GREY.halfAlpha())
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(3)

        val playerCenteredCache = ClientResultCache.getPlayerCached()
        if (playerCenteredCache.isEmpty()) {
            draw.centeredString("no cache received")
        } else {
            for ((_, result) in playerCenteredCache) {
                for ((pos, items) in result) {
                    val block = currentClientPlayer().level().getBlockState(pos).block
                    draw.block(block).move(y = 4).string(block.name).nextLine()
                    for ((slot, item) in items) {
                        val check = draw.inItemRange(pMouseX, pMouseY)
                        if (check) {
                            draw.tooltipForItem(pMouseX, pMouseY, item)
                            draw.tooltipComponent(pMouseX, pMouseY, item.gatherTooltip().append("slot:$slot".literal()))
                        }
                        draw.itemWithDecoration((item)).move(x = 1).move(x = 1)
                    }
                    draw.underLine().nextLine()
                }
            }

        }
    }

    override fun onClose() {
        super.onClose()
        ClientResultCache.cleanCache()
        PlayerCenteredQueryStopPack(currentClientPlayer()).sendToServer()
    }

    override fun isPauseScreen(): Boolean = false
}