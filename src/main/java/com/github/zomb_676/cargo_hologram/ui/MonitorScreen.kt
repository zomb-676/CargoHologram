package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryRequestPack
import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryStopPack
import com.github.zomb_676.cargo_hologram.network.RequestRemoteTake
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.QueryRequirement
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.component.ItemComponent
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.CycleButton
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraftforge.client.gui.widget.ForgeSlider
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sign

class MonitorScreen : Screen("monitor".literal()), CargoBlurScreen {

    private var cursor = AreaImmute.ofFullScreen().asBaseCursor()
    private var currentCount = 1

    var mainArea: AreaImmute = cursor
        private set
    lateinit var sortButton: CycleButton<SortType>
    var hovered: Pair<BlockPos, SlotItemStack>? = null

    init {
        PlayerCenteredQueryRequestPack(
            currentClientPlayer().uuid,
            2,
            QueryRequirement(force = false, crossDimension = false)
        ).sendToServer()
    }

    override fun init() {
        cursor = AreaImmute.ofSize(width, height).asBaseCursor()
        mainArea = cursor.percentX(0.6).percentY(0.8).asAreaImmute()
        sortButton = CycleButton.builder<SortType> {
            it.name.lowercase().literal()
        }.withInitialValue(SortType.IGNORE_CONTAINER)
            .withValues(SortType.entries)
            .displayOnlyValue()
            .create(0, 0, 100, 100, "".literal()) { button, sortType ->

            }
        addRenderableWidget(sortButton)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(pGuiGraphics, mainArea)
        hovered = null
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(3)
        draw.assignUp(25).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Presets.WHITE)
            draw.inner(3)
            draw.assignLeft(100, sortButton::assign)
        }
        draw.innerY(3)
        draw.outline(ARGBColor.Presets.WHITE)

        val sliderArea = draw.assignRight(10)

        draw.inner(2).moveRight(2)
        val widthCount = draw.width / UIConstant.ITEM_SIZE_WITH_PADDING
        val heightCount = draw.height / UIConstant.ITEM_SIZE_WITH_PADDING
        draw.innerX((draw.width - widthCount * UIConstant.ITEM_SIZE_WITH_PADDING) / 2)
        draw.innerY((draw.height - heightCount * UIConstant.ITEM_SIZE_WITH_PADDING) / 2)
        var drawCount = 0

        if (ClientResultCache.isEmpty()) {
            draw.centeredString("no cache received")
        } else {
            sliderArea.draw(pGuiGraphics) { draw ->
                draw.inner(2)
                draw.outline((ARGBColor.Presets.WHITE))
                draw.inner(2)
                val totalCount = ClientResultCache.count()
                val actualHeight = ceil(totalCount / widthCount.toDouble()).toInt()
                currentCount = currentCount.coerceIn(1, max(actualHeight - heightCount + 1, 1))
                if (actualHeight <= heightCount) {
                    draw.fill(ARGBColor.Presets.WHITE)
                } else {
                    val oneHeight = 1.0 / actualHeight * draw.height
                    val take = heightCount * oneHeight
                    val up = (currentCount - 1) * oneHeight
                    draw.upDown(up.toInt())
                    draw.fill(draw.width, take.toInt(), ARGBColor.Presets.WHITE)
                }
            }

            draw.newAnchor()
            var skipCount = -(currentCount - 1) * widthCount
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
                        }
                        draw.move(1, 1).itemWithDecoration(item).move(1, -1)
                    }
                }
            }
        }
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        hovered?.let { (pos, slotItem) ->
            val shiftDown = InteractHelper.currentModifiers().isShiftDown
            val takeCount = if (shiftDown) slotItem.itemStack.count else 1
            RequestRemoteTake(
                takeCount,
                slotItem,
                pos,
                currentClientPlayer().level().dimension(),
                UUID.randomUUID()
            ).sendToServer()
            return true
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    override fun onClose() {
        super.onClose()
        ClientResultCache.cleanCache()
        PlayerCenteredQueryStopPack(currentClientPlayer()).sendToServer()
    }

    override fun isPauseScreen(): Boolean = false

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        currentCount -= pDelta.toInt().sign
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }
}