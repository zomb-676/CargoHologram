package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.util.currentMcFont
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

class CargoTextBox : EditBox(currentMcFont(), 0, 0, 0, 0, Component.empty()) {

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }
}