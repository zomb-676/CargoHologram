package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.currentMcFont
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraftforge.client.gui.widget.ForgeSlider
import java.util.function.DoubleConsumer

class CargoSlider(
    prefix: Component, suffix: Component, min: Double, max: Double, initial: Double, step: Double,
    precession: Int, display: Boolean,
) : ForgeSlider(0, 0, 0, 0, prefix, suffix, min, max, initial, step, precession, display) {
    companion object {
        fun ofRange(from: Double, to: Double, initial: Double) =
            CargoSlider(Component.empty(), Component.empty(), from, to, initial, 1.0, 0, true)

        fun ofRange(from: Int, to: Int, initial: Int) = ofRange(from.toDouble(), to.toDouble(), initial.toDouble())
    }

    private val listeners = mutableListOf<DoubleConsumer>()

    override fun renderWidget(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        val alpha = if (isHovered()) 0x75 else 0xff
        guiGraphics.renderOutline(x, y, width, height, ARGBColor.Presets.WHITE.color)

        AreaImmute.ofRelative(x + (value * (width - 8).toDouble()).toInt(), y, 8, height).asBaseCursor().draw(guiGraphics) {draw ->
            draw.innerY(2)
            draw.outline(ARGBColor.Vanilla.WHITE.alpha(alpha))
            if (this.isHovered()) {
                draw.inner(2)
                draw.fill(ARGBColor.Vanilla.WHITE.alpha(0x9f))
            }
        }
        renderScrollingString(guiGraphics, currentMcFont(), 2, ARGBColor.Vanilla.WHITE.color)
    }

    fun withValueListener(valueListener: DoubleConsumer): CargoSlider {
        listeners.add(valueListener)
        return this
    }

    override fun updateMessage() {
        super.updateMessage()
        //this can be called during object instance, only super obj is initialized
        @Suppress("UNNECESSARY_SAFE_CALL")
        listeners?.forEach{
            it.accept(this.getValue())
        }
    }
}