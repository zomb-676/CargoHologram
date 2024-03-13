package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.network.SetFilterPack
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.widget.CargoButton
import com.github.zomb_676.cargo_hologram.ui.widget.CargoCheckBox
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.assign
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.fillRelative
import com.github.zomb_676.cargo_hologram.util.filter.ItemTrait
import com.github.zomb_676.cargo_hologram.util.filter.SpecifiedItemTrait
import com.github.zomb_676.cargo_hologram.util.filter.TraitList
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.Rect2i
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.item.ItemStack
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class FilterScreen(menu: FilterMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<FilterMenu>(menu, inv, component), CargoBlurScreen {

    private var hoverOnCandidate = false
    private var mainArea: AreaImmute = AreaImmute.ofFullScreen()
    private var currentTopIndex = 0
    private lateinit var modeCheckBox: CargoCheckBox
    private lateinit var setButton: CargoButton
    private lateinit var clearButton: CargoButton

    private var traitIndex = -1
    private var data: List<ItemTrait> = mutableListOf()
    private var toSetTrait = -1
    private var isDeleteMode = false

    override fun init() {
        super.init()
        topPos = (height - 216) / 2
        mainArea = AreaImmute.ofRelative(leftPos, topPos, 176, 216).asAreaImmute()
        modeCheckBox = CargoCheckBox.ofExplicit()
        addRenderableWidget(modeCheckBox)
        setButton = CargoButton.of(UIConstant.Paths.widgetNext)
            .withListeners(::sendSetTraitPack)
        addRenderableWidget(setButton)
        clearButton = CargoButton.of(UIConstant.Paths.widgetRemove)
            .withListeners { SetFilterPack(Optional.empty()).sendToServer() }
        addRenderableWidget(clearButton)
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
        this.traitIndex = -1
        this.isDeleteMode = false
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
        val displayAlready: Boolean
        val traits = TraitList().apply { readFromItem(menu.playerInv.getSelected()) }
        draw.assignUp(22).draw(pGuiGraphics) { draw ->
            draw.outline(ARGBColor.Vanilla.WHITE)
            draw.assignLeft(22)
            if (candidate.isEmpty) {
                draw.centeredString("place item to specific")
                if (!TraitList.contains(menu.playerInv.getSelected())) return
                if (traits.traits.isNotEmpty()) {
                    displayAlready = true
                    this.isDeleteMode = true
                } else return
            } else {
                data = this.collectAvailableForItemStack(candidate)
                draw.centeredString("find ${data.size} trait")
                displayAlready = false
            }
        }

        val showCount = (draw.height) / (2 + 14)
        val dataCount = if (displayAlready) traits.traits.size else data.size
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
                val queryIndex = currentTopIndex + index
                if (draw.inRange(pMouseX, pMouseY)) {
                    this.traitIndex = queryIndex
                }
                draw.outline(ARGBColor.Vanilla.WHITE)

                if (this.isDeleteMode && draw.inRange(pMouseX, pMouseY)) {
                    draw.inner(2)
                    draw.fill(ARGBColor.Vanilla.WHITE.alpha(0x55))
                    draw.expand(2)
                } else if (!this.isDeleteMode && this.toSetTrait == queryIndex) {
                    draw.inner(2)
                    draw.fill(ARGBColor.Vanilla.WHITE.alpha(0x55))
                    draw.expand(2)
                }
                val current =
                    if (displayAlready) traits.traits[queryIndex].description else data[queryIndex].description(
                        candidate
                    )
                draw.assignLeft(16).draw(pGuiGraphics) { draw ->
                    draw.centeredString((queryIndex + 1).toString())
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
            modeCheckBox.assign(draw.cursor)
            draw.outline(ARGBColor.Presets.WHITE)
            if (draw.inRange(pMouseX, pMouseY)) {
                val tooltip = when (modeCheckBox.state) {
                    CargoCheckBox.State.DEFAULT -> throw RuntimeException()
                    CargoCheckBox.State.BANNED -> "Opposite"
                    CargoCheckBox.State.CHECKED -> "Same"
                }.literal()
                draw.tooltipComponent(pMouseX, pMouseY, tooltip)
                draw.inner(2)
                draw.fill(ARGBColor.Presets.WHITE.alpha(0x55))
            }
        }
        sideArea.upDown(2).assignUp(18).draw(pGuiGraphics) { draw ->
            setButton.assign(draw.cursor)
            draw.outline(ARGBColor.Presets.WHITE)
            if (draw.inRange(pMouseX, pMouseY)) {
                draw.tooltipComponent(pMouseX, pMouseY, "Add".literal())
                draw.inner(2)
                draw.fill(ARGBColor.Presets.WHITE.alpha(0x55))
            }
        }

        sideArea.upDown(2).assignUp(18).draw(pGuiGraphics) { draw ->
            clearButton.assign(draw.cursor)
            draw.outline(ARGBColor.Presets.WHITE)
            if (draw.inRange(pMouseX, pMouseY)) {
                draw.tooltipComponent(pMouseX, pMouseY, "Clear All Set".literal())
                draw.inner(2)
                draw.fill(ARGBColor.Presets.WHITE.alpha(0x55))
            }
        }

        for (renderable in this.renderables) {
            renderable.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
        }
    }

    private fun collectAvailableForItemStack(itemStack: ItemStack): List<ItemTrait> {
        val list = ItemTrait.collect(itemStack)
        return list
    }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        currentTopIndex -= pDelta.toInt().sign
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }

    fun getCandidateArea(): Rect2i {
        val slot = menu.candidateSlot
        return Rect2i(leftPos + slot.x, topPos + slot.y, 16, 16)
    }

    override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
        if (traitIndex != -1) {
            if (this.isDeleteMode) {
                val traitList = TraitList().apply { readFromItem(menu.playerInv.getSelected()) }
                traitList.traits.removeAt(traitIndex)
                SetFilterPack(traitList).sendToServer()
                return true
            } else {
                toSetTrait = traitIndex
                return true
            }
        }
        return super.mouseClicked(pMouseX, pMouseY, pButton)
    }

    private fun sendSetTraitPack() {
        val trait = data.getOrNull(toSetTrait) ?: return
        var mode = trait.test(menu.candidateSlot.item)
        if (modeCheckBox.state == CargoCheckBox.State.BANNED) mode = !mode
        val specifiedItemTrait = SpecifiedItemTrait(mode, trait)
        val filter = menu.playerInv.getSelected()
        val traitList = if (TraitList.contains(filter)) {
            val list = TraitList()
            list.deserializeNBT(filter.tag!!)
            list
        } else {
            TraitList()
        }
        traitList.appendTrait(specifiedItemTrait)
        SetFilterPack(traitList).sendToServer()
    }
}