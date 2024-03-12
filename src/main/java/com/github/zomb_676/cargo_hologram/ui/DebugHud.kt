package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.trace.monitor.MonitorCenter
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.entity.EquipmentSlot
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
        if (gui.minecraft.options.renderDebug) return
        if (isOnProduct()) return
        val draw = AreaImmute.ofFullScreen().asBaseCursor().inner(20).forDraw(guiGraphics)

        MonitorCenter.queryMap.forEach { (level, data) ->
            draw.string("level:${level.location()}, have ${data.size} entries")
            draw.nextLine()
        }
    }
}