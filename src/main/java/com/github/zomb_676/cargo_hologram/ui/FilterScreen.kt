package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.filter.ItemTrait
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class FilterScreen(menu: FilterMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<FilterMenu>(menu, inv, component), CargoBlurScreen {

    var hoverOnCandidate = false
        private set
    var mainArea: AreaImmute = AreaImmute.ofFullScreen()
    private var currentTopIndex = 0

    override fun init() {
        super.init()
        topPos = min(20, (topPos * 0.6).toInt())
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176, 216).asAreaImmute()
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        BlurConfigure.render(pGuiGraphics, mainArea)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)
        this.hoveredSlot = null
        this.hoverOnCandidate = false
        menu.slots.forEach { slot ->
            val x = slot.x + leftPos
            val y = slot.y + topPos
            pGuiGraphics.renderOutline(x - 1, y - 1, 18, 18, ARGBColor.Presets.WHITE.color)
            pGuiGraphics.renderItem(slot.item, x, y)
            pGuiGraphics.renderItemDecorations(minecraft!!.font, slot.item, x, y)
            if (this.isHovering(slot.x, slot.y, 16, 16, pMouseX.toDouble(), pMouseY.toDouble())) {
                this.hoveredSlot = slot
                pGuiGraphics.fill(x, y, x + 16, y + 16, ARGBColor.Presets.GREY.color)
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
        val candidateSlot = menu.candidateSlot
        val candidate = menu.candidateHandle.getStackInSlot(0)
        this.hoverOnCandidate =
            this.isHovering(candidateSlot.x, candidateSlot.y, 16, 16, pMouseX.toDouble(), pMouseY.toDouble())
        if (!candidate.isEmpty) {
            pGuiGraphics.renderItem(candidate, candidateSlot.x + leftPos, candidateSlot.y + topPos)
            if (hoverOnCandidate) {
                pGuiGraphics.renderTooltip(minecraft!!.font, candidate, pMouseX, pMouseY)
            }
        }
        draw.downUp(80).inner(3)
        draw.outline(ARGBColor.Vanilla.WHITE)
        draw.inner(2)
        val data: MutableList<Component>
        draw.assignUp(22).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Vanilla.WHITE)
            draw.assignLeft(22)
            if (candidate.isEmpty) {
                draw.centeredString("place item to specific")
                return
            } else {
                data = this.collectAvailableForItemStack(candidate)
                draw.centeredString("find ${data.size} trait")
            }
        }
        val showCount = (draw.height) / (2 + 14)
        val dataCount = data.size
        draw.assignRight(8).draw(pGuiGraphics) { draw ->
            draw.inner(1)
            draw.outline(ARGBColor.Presets.WHITE)
            draw.inner(2)
            currentTopIndex = currentTopIndex.coerceIn(0, max(dataCount - showCount, 0))
            if (showCount >= dataCount) {
                draw.fill(ARGBColor.Presets.WHITE)
            } else {
                val oneHeight = draw.height.toDouble() / dataCount
                val take = showCount * oneHeight
                val up = (currentTopIndex) * oneHeight
                draw.upDown(up.toInt())
                draw.fill(draw.width, take.toInt(), ARGBColor.Presets.WHITE)
            }
        }
        draw.upDown((draw.height - showCount * (2 + 14)) / 2)
        repeat(min(showCount, dataCount)) { index ->
            draw.upDown(2).assignUp(14).draw(pGuiGraphics) { draw ->
                draw.outline(ARGBColor.Vanilla.WHITE)
                val current = data[currentTopIndex + index]
                draw.assignLeft(16).draw(pGuiGraphics) { draw ->
                    draw.centeredString((currentTopIndex + index + 1).toString())
                }
                draw.innerX(2)
                draw.scrollingString(current, ARGBColor.Presets.WHITE)
            }
        }

        val sideArea = mainArea.asBaseCursor()
            .run { rightLeft(width) }
            .leftLeft(20)
            .rightLeft(2)
            .forDraw(pGuiGraphics)
        sideArea.assignUp(18).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Presets.WHITE)
            draw.fillCargoWidget(UIConstant.Paths.checkboxChecked)
        }
    }

    private fun collectAvailableForItemStack(itemStack: ItemStack): MutableList<Component> {
        val list = ItemTrait.collect(itemStack)
        return list.values.toMutableList()
    }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        currentTopIndex -= pDelta.toInt().sign
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }

    fun getCandidateArea(): Rect2i {
        val slot = menu.candidateSlot
        return Rect2i(leftPos + slot.x, topPos + slot.y, 16, 16)
    }

    override fun onClose() {
        super.onClose()
    }
}