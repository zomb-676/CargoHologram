package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.ui.CommandDSL
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.ForgeRegistries

object AllCommands : BusSubscribe {
    override fun registerEvent(modBus: IEventBus, forgeBus: IEventBus) {
        forgeBus.addListener<RegisterCommandsEvent> { event ->
            CommandDSL(event.dispatcher).apply {
                CargoHologram.MOD_ID {
                    "global_selectors" {
                        execute {
                            val filter = Config.Server.globalFilter
                            source.sendSystemMessage("mode:${filter.mode}".literal())
                            filter.selectors.forEach { selector ->
                                source.sendSystemMessage("type:${ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(selector.type)!!}".literal())
                                if (selector.slotSelectors.isNotEmpty()) {
                                    source.sendSystemMessage("selector:${selector.slotSelectors.joinToString(",")}".literal())
                                }
                            }
                        }
                    }
                }
            }
        }
        forgeBus.addListener<RegisterClientCommandsEvent> { event ->

        }
    }

}