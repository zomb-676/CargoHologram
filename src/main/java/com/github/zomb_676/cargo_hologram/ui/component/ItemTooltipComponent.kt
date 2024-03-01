package com.github.zomb_676.cargo_hologram.ui.component

import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.world.item.ItemStack

class ItemTooltipComponent(private val item: ItemStack, private val decoration: Boolean = false) : ClientTooltipComponent {

    override fun getHeight(): Int = 16

    override fun getWidth(pFont: Font): Int = 16

    override fun renderImage(pFont: Font, pX: Int, pY: Int, pGuiGraphics: GuiGraphics) {
        pGuiGraphics.renderItem(item, pX, pY)
        if (decoration) {
            pGuiGraphics.renderItemDecorations(pFont, item, pX, pY)
        }
    }
}