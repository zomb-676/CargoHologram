package com.github.zomb_676.cargo_hologram.util.cursor

import com.github.zomb_676.cargo_hologram.ui.UIConstant
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.MoveData
import com.github.zomb_676.cargo_hologram.util.asItemStack
import com.github.zomb_676.cargo_hologram.util.optional
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import java.util.*

class GraphicCursor<T : Cursor<T>>(private val cursor: Cursor<T>, private val guiGraphics: GuiGraphics) :
    IArea by cursor, AreaTransform<T> by cursor {

    private val moveData = MoveData(0, 0)
    private var lineSpan: Int = 2
    private var autoMove: Boolean = true

    companion object {
        private val FONT = Minecraft.getInstance().font
        private val FONT_HEIGHT = FONT.lineHeight
        private val HALF_FONT_HEIGHT = FONT_HEIGHT / 2
        private const val ADDITION_ITEM_PADDING = 1
        private var ITEM_SPAN = UIConstant.ITEM_SIZE
    }

    override fun isolate(): T = cursor.isolate()

    private val modifyX get() = moveData.x
    private val modifyY get() = moveData.y

    fun fill(color: ARGBColor): GraphicCursor<T> {
        guiGraphics.fill(cursor.x1 + modifyX, cursor.y1 + modifyY, cursor.x2, cursor.y2, color.color)
        return this
    }

    fun fill(sizeX: Int, sizeY: Int = sizeX, color: ARGBColor = ARGBColor.Presets.GREY): GraphicCursor<T> {
        val baseX = cursor.x1 + modifyX
        val baseY = cursor.y1 + modifyY
        guiGraphics.fill(baseX, baseY, baseX + sizeX, baseY + sizeY, color.color)
        return this
    }

    fun outline(color: ARGBColor): GraphicCursor<T> {
        guiGraphics.renderOutline(
            cursor.x1 + modifyX, cursor.y1 + modifyY, cursor.width - modifyX, cursor.height - modifyY, color.color
        )
        return this
    }

    fun outline(width: Int, height: Int = width, color: ARGBColor): GraphicCursor<T> {
        guiGraphics.renderOutline(
            cursor.x1 + modifyX, cursor.y1 + modifyY, width, height, color.color
        )
        return this
    }

    fun centeredString(str: String, color: ARGBColor = ARGBColor.Presets.WHITE): GraphicCursor<T> {
        guiGraphics.drawCenteredString(
            FONT, str, (x1 + modifyX + x2) / 2, ((y1 + modifyY + y2) / 2) - HALF_FONT_HEIGHT, color.color
        )
        return this
    }

    /**
     * @return the width of the [str].
     */
    fun string(str: String, color: ARGBColor = ARGBColor.Presets.WHITE): GraphicCursor<T> {
        val i = x1 + modifyX
        val width = guiGraphics.drawString(FONT, str, i, y1 + modifyY, color.color) - i
        onAutoMove { moveData.appendX(width).spanY(FONT_HEIGHT) }
        return this
    }

    fun string(str: Component, color: ARGBColor = ARGBColor.Presets.WHITE): GraphicCursor<T> {
        val width = guiGraphics.drawString(FONT, str, x1 + modifyX, y1 + modifyY, color.color)
        onAutoMove { moveData.appendX(width).spanY(FONT_HEIGHT) }
        return this
    }

    fun block(block: Block): GraphicCursor<T> = item(block.asItemStack())

    fun item(item: ItemStack): GraphicCursor<T> {
        guiGraphics.renderItem(item, x1 + modifyX, y1 + modifyY)
        onAutoMove { moveData.appendX(ITEM_SPAN).spanY(ITEM_SPAN) }
        return this
    }

    fun itemWithDecoration(item: ItemStack): GraphicCursor<T> {
        val xPos = x1 + modifyX
        val yPos = y1 + modifyY
        guiGraphics.renderItem(item, xPos, yPos)
        guiGraphics.renderItemDecorations(FONT, item, xPos, yPos)
        onAutoMove { moveData.appendX(ITEM_SPAN).spanY(ITEM_SPAN) }
        return this
    }

    fun inItemRange(posX: Int, posY: Int) =
        posX >= x1 + modifyX - ADDITION_ITEM_PADDING && posX <= x1 + modifyX + ITEM_SPAN + ADDITION_ITEM_PADDING
                && posY >= y1 + modifyY - ADDITION_ITEM_PADDING && posY <= y1 + modifyY + ITEM_SPAN + ADDITION_ITEM_PADDING

    fun itemArea() = AreaImmute.ofRelative(
        x1 + modifyX - ADDITION_ITEM_PADDING,
        y1 + modifyY - ADDITION_ITEM_PADDING,
        ITEM_SPAN + (ADDITION_ITEM_PADDING * 2),
        ITEM_SPAN + (ADDITION_ITEM_PADDING * 2)
    )

    fun underLine(color: ARGBColor = ARGBColor.Presets.WHITE): GraphicCursor<T> {
        guiGraphics.hLine(x1 + moveData.anchorX, x1 + modifyX, y1 + modifyY + moveData.spanY, color.color)
        return this
    }

    fun tooltipForItem(x: Int, y: Int, item: ItemStack): GraphicCursor<T> {
        guiGraphics.renderTooltip(FONT, item, x, y)
        return this
    }

    fun tooltipComponent(posX: Int, posY: Int, text: Component): GraphicCursor<T> {
        guiGraphics.renderTooltip(FONT, text, posX, posY)
        return this
    }

    fun tooltipComponent(posX: Int, posY: Int, text: List<Component>) {
        guiGraphics.renderTooltip(FONT, text, Optional.empty(), posX, posY)
    }

    fun tooltipComponent(posX: Int, posY: Int, text: List<Component>, custom: TooltipComponent) {
        guiGraphics.renderTooltip(FONT, text, custom.optional(), posX, posY)
    }


    //
    fun move(x: Int = 0, y: Int = 0): GraphicCursor<T> {
        this.moveData.appendX(x).appendY(y)
        return this
    }

    fun lineSpan(span: Int): GraphicCursor<T> {
        this.lineSpan = span
        return this
    }

    fun autoMove(enable: Boolean = true): GraphicCursor<T> {
        this.autoMove = enable
        return this
    }

    fun nextLine(): GraphicCursor<T> {
        this.moveData.move()
        return this
    }

    fun newAnchor(): GraphicCursor<T> {
        this.moveData.newAnchor()
        return this
    }

    fun toAnchor(): GraphicCursor<T> {
        this.moveData.toAnchor()
        return this
    }

    fun clearAnchor(): GraphicCursor<T> {
        this.moveData.clearAnchor()
        return this
    }

    fun haveSpace(addX: Int = 0, addY: Int = 0) =
        this.containsPointIncludingEdge(x1 + modifyX + addX, y1 + modifyY + addY)

    fun subArea(width: Int, height: Int = width): AreaImmute =
        AreaImmute.ofRelative(x1 + modifyX, y1 + modifyY, width, height)

    private inline fun onAutoMove(codeBlock: () -> Unit) {
        if (this.autoMove) {
            codeBlock()
        }
    }

}