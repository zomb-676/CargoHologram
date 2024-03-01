package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.ui.DebugHud
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.literal
import com.mojang.brigadier.arguments.BoolArgumentType
import net.minecraftforge.registries.ForgeRegistries

object AllCommands : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) = dispatcher.registerCommand {
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
            "debug_overlay" {
                "enable"(BoolArgumentType.bool()) {
                    execute {
                        val enable  = this.getArgument<Boolean>("enable")
                        val current = DebugHud.enable
                        if (enable == current) {
                            source.sendSystemMessage("DebugHud already:$enable".literal())
                        } else {
                            source.sendSystemMessage("DebugHud changed to $enable".literal())
                            DebugHud.enable = current
                        }
                    }
                }
                execute {
                    val enable = DebugHud.enable
                    source.sendSystemMessage("DebugHud:$enable".literal())
                }
            }
        }

    }

}