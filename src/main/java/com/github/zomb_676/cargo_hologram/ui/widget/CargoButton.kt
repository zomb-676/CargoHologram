package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.CargoHologramSpriteUploader
import com.github.zomb_676.cargo_hologram.util.AtlasHandle
import com.github.zomb_676.cargo_hologram.util.isIn
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.resources.ResourceLocation

open class CargoButton protected constructor(
    private val normal: ResourceLocation,
    private val hover: ResourceLocation,
) : AbstractWidget(0, 0, 0, 0, "".literal()) {

    companion object {
        fun of(path: ResourceLocation) = CargoButton(path, path)
        fun of(normal: ResourceLocation, hover: ResourceLocation) = CargoButton(normal, hover)
    }

    protected val listeners: MutableList<() -> Unit> = mutableListOf()

    override fun renderWidget(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val path = if (this.isHovered()) hover else normal
        val sprite = AtlasHandle.query(CargoHologramSpriteUploader.ATLAS_LOCATION).getSprite(path)
        pGuiGraphics.blit(x, y, 0, width, height, sprite)
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {}

    open fun withListeners(f: () -> Unit): CargoButton {
        listeners.add(f)
        return this
    }

    override fun clicked(pMouseX: Double, pMouseY: Double): Boolean {
        if (!isIn(pMouseX, pMouseY)) return false
        listeners.forEach { it.invoke() }
        return true
    }
}