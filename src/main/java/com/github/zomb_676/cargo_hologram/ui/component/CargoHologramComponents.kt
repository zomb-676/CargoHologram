package com.github.zomb_676.cargo_hologram.ui.component

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher

object CargoHologramComponents : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher.registerTooltipComponent { component: ItemComponent ->
            ItemTooltipComponent(component.item, component.decoration)
        }
    }
}