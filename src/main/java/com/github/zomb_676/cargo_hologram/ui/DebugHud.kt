package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.currentMinecraft
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.isOnDev
import net.minecraft.client.gui.GuiGraphics
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay

object DebugHud : BusSubscribe, IGuiOverlay {

    var enable = isOnDev()

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<RegisterGuiOverlaysEvent> { event ->
            event.registerAboveAll("debug_overlay", this)
        }
    }

    override fun render(
        gui: ForgeGui,
        guiGraphics: GuiGraphics,
        partialTick: Float,
        screenWidth: Int,
        screenHeight: Int,
    ) {
        if (currentMinecraft().options.renderDebug) return
        val drawer = AreaImmute.ofSize(screenWidth, screenHeight).asBaseCursor().forDraw(guiGraphics)

    }
}