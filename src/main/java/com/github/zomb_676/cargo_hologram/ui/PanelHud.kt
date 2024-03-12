package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import net.minecraft.client.gui.GuiGraphics
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay

object PanelHud : BusSubscribe, IGuiOverlay {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<RegisterGuiOverlaysEvent> { event ->
            event.registerAboveAll("glass_monitor_hud", this)
        }
    }

    override fun render(
        gui: ForgeGui,
        guiGraphics: GuiGraphics,
        partialTick: Float,
        screenWidth: Int,
        screenHeight: Int,
    ) {

    }
}