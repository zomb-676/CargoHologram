package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.RequestRemoteTake
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.component.ItemComponent
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sign

class CraftScreen(menu: CraftMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<CraftMenu>(menu, inv, component), CargoBlurScreen {

    private var currentRowIndex = 0
    private val materialAreas = MutableList(9) { _ -> AreaImmute.ofFullScreen() }
    var hovered: Pair<BlockPos, SlotItemStack>? = null
    var area: AreaImmute? = null

    var mainArea: AreaImmute = AreaImmute.ofFullScreen()
        private set

    override fun init() {
        super.init()
        this.leftPos -= 25
        this.topPos = (height - 216) / 2
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176 + 59, 216).asAreaImmute()
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        BlurConfigure.render(pGuiGraphics, mainArea)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        this.hovered = null
        this.renderBg(pGuiGraphics, pPartialTick, pMouseX, pMouseY)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Vanilla.WHITE)
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
        val monitorArea = draw
            .downUp(80)
            .inner(3)
            .forDraw(pGuiGraphics)
        monitorArea.outline(ARGBColor.Vanilla.WHITE)

        val sideArea = monitorArea.assignRight(10)

        val widthCount = monitorArea.width / UIConstant.ITEM_SIZE_WITH_PADDING
        val heightCount = monitorArea.height / UIConstant.ITEM_SIZE_WITH_PADDING
        monitorArea.innerX((monitorArea.width - widthCount * UIConstant.ITEM_SIZE_WITH_PADDING) / 2)
        monitorArea.innerY((monitorArea.height - heightCount * UIConstant.ITEM_SIZE_WITH_PADDING) / 2)
        var drawCount = 0

        if (ClientResultCache.isEmpty()) {
            monitorArea.centeredString("no cache received")
        } else {
            sideArea.draw(pGuiGraphics) { draw ->
                draw.inner(2)
                draw.outline((ARGBColor.Presets.WHITE))
                draw.inner(2)
                val totalHeight = ClientResultCache.count()
                val showHeight = ceil(totalHeight / widthCount.toDouble()).toInt()
                currentRowIndex = currentRowIndex.coerceIn(1, max(showHeight - heightCount + 1, 1))
                if (showHeight <= heightCount) {
                    draw.fill(ARGBColor.Presets.WHITE)
                } else {
                    val oneRowHeight = 1.0 / showHeight * draw.height
                    val take = heightCount * oneRowHeight
                    val up = (currentRowIndex - 1) * oneRowHeight
                    draw.upDown(up.toInt())
                    draw.fill(draw.width, take.toInt(), ARGBColor.Presets.WHITE)
                }
            }

            draw.newAnchor()
            var skipCount = -(currentRowIndex - 1) * widthCount
            var checked = false
            full@ for ((_, result) in ClientResultCache.iter()) {
                for ((pos, items) in result.iterBy()) {
                    val block = currentClientPlayer().level().getBlockState(pos).block
                    slot@ for ((slot, item) in items) {
                        if (++skipCount <= 0) continue@slot
                        drawCount++
                        if (drawCount > widthCount) {
                            drawCount = 1
                            draw.nextLine().move(y = 2).newAnchor()
                            if (!draw.haveSpace(addY = 1)) break@full
                        }
                        val check = draw.inItemRange(pMouseX, pMouseY)
                        if (check && !checked) {
                            checked = true
                            draw.fill(18, color = ARGBColor.Presets.WHITE.halfAlpha())
                            draw.tooltipComponent(
                                pMouseX,
                                pMouseY,
                                item.gatherTooltip()
                                    .append("slot:$slot".literal())
                                    .append("located:(x:${pos.x},y:${pos.y},z:${pos.z})".literal()),
                                ItemComponent(block)
                            )
                            hovered = pos to SlotItemStack(slot, item)
                            area = draw.itemArea()
                        }
                        draw.move(1, 1).itemWithDecoration(item).move(1, -1)
                    }
                }
            }
        }
    }

    fun craftMaterialSlotsArea(): List<AreaImmute> =
        List(9) { index ->
            val slot = menu.slots[index]
            AreaImmute.ofRelative(slot.x + leftPos, slot.y + topPos, 16, 16)
        }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        currentRowIndex -= pDelta.toInt().sign
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        hovered?.let { (pos, slotItem) ->
            val shiftDown = InteractHelper.currentModifiers().isShiftDown
            val takeCount = if (shiftDown) slotItem.itemStack.count else 1
            RequestRemoteTake(
                takeCount, slotItem, pos, currentClientPlayer().level().dimension(), UUID.randomUUID()
            ).sendToServer()
            return true
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }
}