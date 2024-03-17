package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.InserterTransformPacket
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.widget.CargoButton
import com.github.zomb_676.cargo_hologram.ui.widget.CargoCheckBox
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.fillRelative
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu

class InserterScreen(menu: InserterMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<InserterMenu>(menu, inv, component), CargoBlurScreen {

    private var mainArea: AreaImmute = AreaImmute.ofFullScreen()
    private lateinit var transButton: CargoButton
    private lateinit var highlightBind : CargoCheckBox

    override fun init() {
        super.init()
        this.topPos = (height - 170) / 2
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176, 170).asAreaImmute()
        transButton = CargoButton.of(UIConstant.Paths.widgetSave).withListeners {
            InserterTransformPacket().sendToServer()
        }
        addRenderableWidget(transButton)
        highlightBind = CargoCheckBox.ofImplicit(menu.pos == HighlightLinked.bind).withListener { state ->
            when(state) {
                CargoCheckBox.State.DEFAULT -> {
                    HighlightLinked.bind = BlockPos.ZERO
                    HighlightLinked.blocks = emptyList()
                }
                CargoCheckBox.State.CHECKED -> {
                    HighlightLinked.bind = menu.pos
                    HighlightLinked.blocks = menu.inserter.linked.map { it.second }
                    if (HighlightLinked.blocks.isEmpty()) {
                        menu.playerInv.player.sendSystemMessage("link nothing, no block will be highlighted".literal())
                        highlightBind.switch()
                    }
                }
                CargoCheckBox.State.BANNED -> throw RuntimeException()
            }
        }
        addRenderableWidget(highlightBind)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
    }

    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(this, pGuiGraphics, mainArea)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)
        menu.slots.forEach { slot ->
            val x = slot.x + leftPos
            val y = slot.y + topPos
            pGuiGraphics.fillRelative(x - 1, y - 1, 18, 18, ARGBColor.Presets.GREY.alpha(0x5f))
            var slotItem = slot.item
            if (isQuickCrafting && !menu.carried.isEmpty && quickCraftSlots.contains(slot)) {
                val count = AbstractContainerMenu.getQuickCraftPlaceCount(
                    this.quickCraftSlots,
                    this.quickCraftingType,
                    menu.carried
                )
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
            .moveLeft(2).leftLeft(16).forDraw(pGuiGraphics)

        side.assignUp(16).draw(pGuiGraphics) { draw ->
            draw.cursor.setWidget(transButton)
            if (draw.inRange(pMouseX, pMouseY)) {
                val tips = listOf("transform items".literal(),
                    "will be transformed automatically when ui closed".literal())
                draw.tooltipComponent(pMouseX, pMouseY, tips)
            }
        }

        side.upDown(5)
        side.assignUp(16).draw(pGuiGraphics) { draw ->
            draw.cursor.setWidget(highlightBind)
            if (draw.inRange(pMouseX, pMouseY)) {
                draw.tooltipComponent(pMouseX, pMouseY, "highlight linked blocks".literal())
            }
        }

        this.renderables.forEach { renderable ->
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        }
    }
}