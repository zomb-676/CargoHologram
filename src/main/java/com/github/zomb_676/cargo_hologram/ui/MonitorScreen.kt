package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryRequestPack
import com.github.zomb_676.cargo_hologram.network.PlayerCenteredQueryStopPack
import com.github.zomb_676.cargo_hologram.network.RequestRemoteTake
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.request.QueryRequirement
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.component.ItemComponent
import com.github.zomb_676.cargo_hologram.ui.widget.CargoCycleButton
import com.github.zomb_676.cargo_hologram.ui.widget.CargoTextBox
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import java.util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sign

class MonitorScreen : Screen("monitor".literal()), CargoBlurScreen {

    private var cursor = AreaImmute.ofFullScreen().asBaseCursor()
    private var currentRowIndex = 1

    var mainArea: AreaImmute = cursor
        private set
    private var hovered: Pair<BlockPos, SlotItemStack>? = null
    private lateinit var searchBar: CargoTextBox
    private lateinit var backedButton: CargoCycleButton<SearchEngine.Type>

    init {
        PlayerCenteredQueryRequestPack(
            currentClientPlayer().uuid, 2, QueryRequirement(force = false, crossDimension = false)
        ).sendToServer()
    }

    override fun init() {
        cursor = AreaImmute.ofSize(width, height).asBaseCursor()
        mainArea = cursor.percentX(0.4).percentY(0.8).asAreaImmute()
        searchBar = CargoTextBox()
        this.addRenderableWidget(searchBar)
        backedButton = CargoCycleButton.of(UIConstant.Paths.cycleButton, SearchEngine.getBackedType())
            .withListeners { SearchEngine.setBacked(backedButton.currentState()) }
        this.addRenderableWidget(backedButton)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(this, pGuiGraphics, mainArea)
        hovered = null
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.outline(ARGBColor.Presets.WHITE)

        draw.assignUp(25).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Presets.WHITE)
            draw.inner(4)
            draw.assignLeft(100, searchBar::assign)
        }

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
            draw.newAnchor()
            var skipCount = -(currentRowIndex - 1) * widthCount
            var checked = false
//            full@ for ((_, result) in ClientResultCache.iter()) {
//                for ((pos, items) in result.iterBy()) {
//                    val block = currentClientPlayer().level().getBlockState(pos).block
//                    slot@ for ((slot, item) in items) {
//                        if (++skipCount <= 0) continue@slot
//                        drawCount++
//                        if (drawCount > widthCount) {
//                            drawCount = 1
//                            draw.nextLine().move(y = 2).newAnchor()
//                            if (!draw.haveSpace(addY = 1)) break@full
//                        }
//                        val check = draw.inItemRange(pMouseX, pMouseY)
//                        if (check && !checked) {
//                            checked = true
//                            draw.fill(18, color = ARGBColor.Presets.WHITE.halfAlpha())
//                            draw.tooltipComponent(
//                                pMouseX,
//                                pMouseY,
//                                item.gatherTooltip().append("slot:$slot".literal())
//                                    .append("located:(x:${pos.x},y:${pos.y},z:${pos.z})".literal()),
//                                ItemComponent(block)
//                            )
//                            hovered = pos to SlotItemStack(slot, item)
//                        }
//                        draw.move(1, 1).itemWithDecoration(item).move(1, -1)
//                    }
//                }
//            }

            val totalCount = let {
                return@let ClientResultCache.iter(searchBar.value) { pos, block, slot, item ->
                    if (++skipCount <= 0) return@iter
                    drawCount++
                    if (drawCount > widthCount) {
                        drawCount = 1
                        draw.nextLine().move(y = 2).newAnchor()
                        if (!draw.haveSpace(addY = 1)) return@iter
                    }
                    val check = draw.inItemRange(pMouseX, pMouseY)
                    if (check && !checked) {
                        checked = true
                        draw.fill(18, color = ARGBColor.Presets.WHITE.halfAlpha())
                        draw.tooltipComponent(
                            pMouseX,
                            pMouseY,
                            item.gatherTooltip().append("slot:$slot".literal())
                                .append("located:(x:${pos.x},y:${pos.y},z:${pos.z})".literal()),
                            ItemComponent(block)
                        )
                        hovered = pos to SlotItemStack(slot, item)
                    }
                    draw.move(1, 1).itemWithDecoration(item).move(1, -1)
                }
            }

            sliderArea.draw(pGuiGraphics) { draw ->
                draw.inner(2)
                draw.outline((ARGBColor.Presets.WHITE))
                draw.inner(2)
                val showHeight = ceil(totalCount / widthCount.toDouble()).toInt()
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
        }

        val side = mainArea.asBaseCursor().apply { rightLeft(width) }.moveLeft(2).leftLeft(16).forDraw(pGuiGraphics)
        side.assignUp(16).draw(pGuiGraphics) { draw ->
            draw.cursor.setWidget(backedButton)
            if (draw.inRange(pMouseX, pMouseY)) {
                val type = backedButton.currentState()
                val message = when(type) {
                    SearchEngine.Type.JEI -> "use and sync with jei search"
                    SearchEngine.Type.SELF -> "mod host search, begin with @ for modID and # for tag"
                }.literal()
                draw.tooltipComponent(pMouseX, pMouseY, message)
            }
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
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

    override fun onClose() {
        super.onClose()
        ClientResultCache.cleanPlayerCenteredCache()
        SearchEngine.save()
        PlayerCenteredQueryStopPack(currentClientPlayer()).sendToServer()
    }

    override fun isPauseScreen(): Boolean = false

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        currentRowIndex -= pDelta.toInt().sign
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }

    override fun keyReleased(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
        val key = InputConstants.getKey(pKeyCode, pScanCode)
        if (focused != searchBar && minecraft!!.options.keyInventory.isActiveAndMatches(key)) {
            this.onClose()
            return true
        }
        return super.keyReleased(pKeyCode, pScanCode, pModifiers)
    }
}