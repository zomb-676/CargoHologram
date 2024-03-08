package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack

class CraftScreen(menu: CraftMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<CraftMenu>(menu, inv, component), CargoBlurScreen {

    private var cursor = AreaImmute.ofFullScreen().asBaseCursor()
    private var currentCount = 1
    private val materialAreas = MutableList(9) { _ -> AreaImmute.ofFullScreen() }
    private var hoveredItem: ItemStack? = null

    var mainArea: AreaImmute = cursor
        private set

    override fun init() {
        cursor = AreaImmute.ofSize(width, height).asBaseCursor()
        mainArea = cursor.percentX(0.6).percentY(0.8).asAreaImmute()
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        BlurConfigure.render(pGuiGraphics, mainArea)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY)
        hoveredItem = null
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.fill(ARGBColor.Presets.GREY.halfAlpha())
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(2)
        draw.assignUp(66).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Presets.WHITE).autoMove(false)
            draw.inner(5)
            for (y in 0..2) {
                draw.newAnchor()
                for (x in 0..2) {

                    val index = x + y * 3
                    materialAreas[index] = draw.subArea(UIConstant.ITEM_SIZE_WITH_PADDING)
                    draw.outline(UIConstant.ITEM_SIZE_WITH_PADDING, color = ARGBColor.Presets.WHITE)
                    menu.materialHandle.getStackInSlot(index).let { item ->
                        if (item.isEmpty) return@let
                        draw.move(1, 1).item(item).move(-1, -1)
                        if (draw.inItemRange(pMouseX, pMouseY)) hoveredItem = item
                    }
                    draw.move(x = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
                }
                draw.toAnchor().move(y = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
            }
        }
        materialAreas[5].let { area ->
            pGuiGraphics.renderOutline(area.x1 + 19, area.y1, 18, 18, ARGBColor.Presets.WHITE.color)
            val result = menu.resultHandle.getStackInSlot(0)
            if (result.isEmpty) return@let
            pGuiGraphics.renderItem(result, area.x1 + 20, area.y1 + 1)
            pGuiGraphics.renderItemDecorations(minecraft!!.font, result, area.x1 + 20, area.y1 + 1)
        }
        draw.upDown(2)
        draw.outline(ARGBColor.Presets.WHITE).inner(5)
        for (y in 0..3) {
            draw.newAnchor().autoMove(false)
            for (x in 0..8) {
                val index = x + y * 9
                draw.outline(UIConstant.ITEM_SIZE_WITH_PADDING, color = ARGBColor.Presets.WHITE)
                val item = menu.getSlot(10 + index).item
                if (draw.inItemRange(pMouseX, pMouseY)) hoveredItem = item
                if (!item.isEmpty) draw.move(1, 1).itemWithDecoration(item).move(-1, -1)
                draw.move(x = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
            }
            draw.toAnchor().move(y = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
            if (y == 2) draw.move(y = 5)
        }
        if (hoveredItem != null && !hoveredItem!!.isEmpty) {
            draw.tooltipForItem(pMouseX, pMouseY, hoveredItem!!)
        }
    }

    fun craftMaterialSlotsArea(): List<AreaImmute> = materialAreas
}