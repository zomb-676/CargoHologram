package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.CargoHologramSpriteUploader
import com.github.zomb_676.cargo_hologram.ui.UIConstant
import com.github.zomb_676.cargo_hologram.util.AtlasHandle
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.resources.ResourceLocation

class CargoCheckBox private constructor(val type: Type, initial: State) : AbstractWidget(0, 0, 0, 0, "".literal()) {
    var state = initial
        private set
    private var index = type.valid.indexOf(state)
    private val listeners: MutableList<(State) -> Unit> = mutableListOf()

    enum class State(val path: ResourceLocation, val value: Boolean) {
        DEFAULT(UIConstant.Paths.checkboxDefault, false),
        BANNED(UIConstant.Paths.checkboxBanned, false),
        CHECKED(UIConstant.Paths.checkboxChecked, true)
    }

    enum class Type(vararg val valid: State) {
        FULL(State.DEFAULT, State.BANNED, State.CHECKED),
        EXPLICIT(State.CHECKED, State.BANNED),
        IMPLICIT(State.DEFAULT, State.CHECKED),
    }

    companion object {
        fun ofFull(state: State = State.DEFAULT) = CargoCheckBox(Type.FULL, state)
        fun ofExplicit(state: State = State.CHECKED) = CargoCheckBox(Type.EXPLICIT, state)
        fun ofImplicit(state: State = State.CHECKED) = CargoCheckBox(Type.IMPLICIT, state)
        fun ofImplicit(state: Boolean) = ofImplicit(if (state) State.CHECKED else State.DEFAULT)
    }

    override fun renderWidget(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val sprite = AtlasHandle.query(CargoHologramSpriteUploader.ATLAS_LOCATION).getSprite(state.path)
        pGuiGraphics.blit(x, y, 0, width, height, sprite)
    }

    override fun clicked(pMouseX: Double, pMouseY: Double): Boolean {
        if (pMouseX < x || pMouseX > x + width || pMouseY < y || pMouseY > y + height) return false
        switch()
        return true
    }

    override fun updateWidgetNarration(pNarrationElementOutput: NarrationElementOutput) {}

    fun withListener(f: (State) -> Unit): CargoCheckBox {
        listeners.add(f)
        return this
    }

    fun switch() {
        index++
        val valid = type.valid
        if (index == valid.size) index = 0
        this.state = valid[index]
        listeners.forEach { it.invoke(this.state) }
    }

}