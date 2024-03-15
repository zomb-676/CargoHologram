package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.currentMcFont
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.DataSlot
import kotlin.math.sign

class CargoValueWidget(val data: DataSlot) : AbstractWidget(0, 0, 0, 0, Component.empty()) {

    var value: Int
        get() = data.get()
        set(value) = data.set(value)

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        value += pDelta.sign.toInt()
        return true
    }

    override fun renderWidget(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        pGuiGraphics.renderOutline(x, y, width, height, ARGBColor.Vanilla.WHITE.color)
        val font = currentMcFont()
        val strY = (y + y + height - font.lineHeight) / 2
        val strX = x + width / 2
        pGuiGraphics.drawCenteredString(currentMcFont(), value.toString(), strX, strY, ARGBColor.Vanilla.WHITE.color)
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {}
}