package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.widget.CargoValueWidget
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.assign
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.fillRelative
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu.getQuickCraftPlaceCount
import net.minecraft.world.item.ItemStack

class CargoStorageScreen(menu: CargoStorageMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<CargoStorageMenu>(menu, inv, component), CargoBlurScreen {

    var mainArea = AreaImmute.ofFullScreen()
        private set
    lateinit var priorityWidget: CargoValueWidget

    override fun init() {
        super.init()
        this.topPos = (height - 170) / 2
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176, 170).asAreaImmute()
        priorityWidget = CargoValueWidget(menu.priorityData)
        addRenderableWidget(priorityWidget)
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(this, pGuiGraphics, mainArea)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)

        pGuiGraphics.hLine(draw.x1, draw.x2 - 1, topPos + (18 + 2) * 4 + 2, ARGBColor.Vanilla.WHITE.color)

        menu.slots.forEach { slot ->
            val x = slot.x + leftPos
            val y = slot.y + topPos
            pGuiGraphics.fillRelative(x - 1, y - 1, 18, 18, ARGBColor.Presets.GREY.alpha(0x5f))
            var slotItem = slot.item
            if (isQuickCrafting && !menu.carried.isEmpty && quickCraftSlots.contains(slot)) {
                val count = getQuickCraftPlaceCount(this.quickCraftSlots, this.quickCraftingType, menu.carried)
                slotItem = if (slotItem.isEmpty) {
                    menu.carried.copyWithCount(count)
                } else {
                    slotItem.copyWithCount(slotItem.count + count)
                }
                pGuiGraphics.fillRelative(x - 1, y - 1, 18, 18, ARGBColor.Presets.GREY)
            }
            pGuiGraphics.renderItem(slotItem, x, y)
            pGuiGraphics.renderItemDecorations(minecraft!!.font, slotItem, x, y)
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

        val side = mainArea.asBaseCursor().apply { rightLeft(width) }
            .moveLeft(2).leftLeft(18).forDraw(pGuiGraphics)

        side.upDown(16 * 2 + 5 + 2)
        side.assignUp(18).draw(pGuiGraphics) { draw ->
            priorityWidget.assign(draw.cursor)
            if (draw.inRange(pMouseX, pMouseY)) {
                val message =
                    listOf("priority:${priorityWidget.value}".literal(), "use mouse scroll to adjust".literal())
                draw.tooltipComponent(pMouseX, pMouseY, message)
            }
        }

        this.renderables.forEach { widget ->
            widget.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        }
    }

    override fun getTooltipFromContainerItem(pStack: ItemStack): MutableList<Component> {
        return super.getTooltipFromContainerItem(pStack)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {}
}