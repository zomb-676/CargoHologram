package com.github.zomb_676.cargo_hologram.util

import net.minecraftforge.eventbus.api.IEventBus

interface BusSubscribe {
    fun registerEvent(modBus: IEventBus, forgeBus: IEventBus)
}