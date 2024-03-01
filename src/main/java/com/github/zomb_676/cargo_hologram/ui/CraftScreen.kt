package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import org.apache.http.util.Asserts

class CraftScreen(menu: CraftMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<CraftMenu>(menu, inv, component) {

    private var cursor = AreaImmute.ofFullScreen().asBaseCursor()
    private var currentCount = 1
    private val materialAreas = MutableList(9) { _ -> AreaImmute.ofFullScreen() }

    var mainArea: AreaImmute = cursor
        private set

    override fun init() {
        cursor = AreaImmute.ofSize(width, height).asBaseCursor()
        mainArea = cursor.percentX(0.6).percentY(0.8).asAreaImmute()
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        this.renderBackground(pGuiGraphics)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.fill(ARGBColor.Presets.GREY.halfAlpha())
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(2)
        draw.assignUp(66).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Presets.WHITE).autoMove(false)
            draw.inner(5)
            for (x in 0..2) {
                draw.newAnchor()
                for (y in 0..2) {

                    val index = x * 3 + y
                    materialAreas[index] = draw.subArea(UIConstant.ITEM_SIZE_WITH_PADDING)
                    draw.fill(UIConstant.ITEM_SIZE_WITH_PADDING)
                    menu.mateiralHandle.getStackInSlot(index).let { item ->
                        if (item.isEmpty) return@let
                        draw.move(1, 1).item(item).move(-1,-1)
                    }
                    draw.move(x = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
                }
                draw.toAnchor().move(y = UIConstant.ITEM_SIZE_WITH_PADDING + 1)
            }
        }
    }

    fun craftMaterialSlotsArea(): List<AreaImmute> = materialAreas

    fun set(area: AreaImmute, ingredient: ItemStack) {
        val index = materialAreas.indexOfFirst { it.isSameArea(area) }
        set(index, ingredient)
    }

    fun set(index: Int, ingredient: ItemStack) {
        Asserts.check(index in 0..8, "index:$index")
        menu.mateiralHandle.setStackInSlot(index, ingredient)
    }
}