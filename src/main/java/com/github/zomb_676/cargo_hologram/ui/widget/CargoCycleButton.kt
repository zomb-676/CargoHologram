package com.github.zomb_676.cargo_hologram.ui.widget

import com.github.zomb_676.cargo_hologram.util.isIn
import net.minecraft.resources.ResourceLocation

class CargoCycleButton<T>(normal: ResourceLocation, hover: ResourceLocation, states: Array<T>, initial: T) :
    CargoButton(normal, hover) {

    companion object {
        inline fun <reified T> of(texture: ResourceLocation, state: Collection<T>): CargoCycleButton<T> {
            if (state.isEmpty()) throw RuntimeException("states must not empty")
            return CargoCycleButton(texture, texture, state.toTypedArray(), state.first())
        }

        inline fun <reified T : Enum<T>> of(texture: ResourceLocation): CargoCycleButton<T> {
            val entries = enumValues<T>()
            if (entries.isEmpty()) throw RuntimeException("enum class ${T::class.java.simpleName} not have any entries")
            return CargoCycleButton(texture, texture, entries, entries.first())
        }

        inline fun <reified T : Enum<T>> of(texture: ResourceLocation, initial: T): CargoCycleButton<T> {
            val entries = enumValues<T>()
            return CargoCycleButton(texture, texture, entries, initial)
        }
    }

    private val state: Array<T> = states
    private var currentState = initial
    private var index = states.indexOf(currentState)

    init {
        if (index == -1) {
            throw RuntimeException("state:$initial is not in valid list:${states.joinToString(separator = ",")}")
        }
    }

    override fun clicked(pMouseX: Double, pMouseY: Double): Boolean {
        if (!isIn(pMouseX, pMouseY)) return false
        index++
        if (index == state.size) {
            index = 0
        }
        this.currentState = state[index]
        listeners.forEach { it.invoke() }
        return true
    }

    fun currentState() = currentState

    override fun withListeners(f: () -> Unit): CargoCycleButton<T> {
        super.withListeners(f)
        return this
    }
}