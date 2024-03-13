package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.fillRelative
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

class CargoStorageScreen(menu: CargoStorageMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<CargoStorageMenu>(menu, inv, component), CargoBlurScreen {

    var mainArea = AreaImmute.ofFullScreen()
        private set

    override fun init() {
        super.init()
        this.topPos = (height - 216) / 2
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176, 216).asAreaImmute()
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(pGuiGraphics, mainArea)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)

        menu.slots.forEach { slot ->
            val x = slot.x + leftPos
            val y = slot.y + topPos
            pGuiGraphics.fillRelative(x - 1, y - 1, 18, 18, ARGBColor.Presets.GREY.alpha(0x5f))
            if (!slot.item.isEmpty) {
                pGuiGraphics.renderItem(slot.item, x, y)
                pGuiGraphics.renderItemDecorations(minecraft!!.font, slot.item, x, y)
            }
            if (this.isHovering(slot.x, slot.y, 16, 16, pMouseX.toDouble(), pMouseY.toDouble())) {
                this.hoveredSlot = slot
                pGuiGraphics.fillRelative(x - 1, y - 1, 18, 18, ARGBColor.Presets.GREY)
                if (!slot.item.isEmpty)
                    pGuiGraphics.renderTooltip(minecraft!!.font, slot.item, pMouseX, pMouseY)
            }
        }
        val carried = menu.carried
        if (!carried.isEmpty) {
            val pose = pGuiGraphics.pose()
            pose.pushPose()
            pose.translate(0.0f, 0.0f, 232.0f)
            pGuiGraphics.renderItem(carried, pMouseX - 8, pMouseY - 8)
            pGuiGraphics.renderItemDecorations(minecraft!!.font, carried, pMouseX - 8, pMouseY - 8)
            pose.popPose()
        }
    }

    override fun getTooltipFromContainerItem(pStack: ItemStack): MutableList<Component> {
        return super.getTooltipFromContainerItem(pStack)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {}
}