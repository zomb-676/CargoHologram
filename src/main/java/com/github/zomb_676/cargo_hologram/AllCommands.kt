package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.network.TransformPlayerInvToNearbyPack
import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
import com.github.zomb_676.cargo_hologram.ui.ConfigureScreen
import com.github.zomb_676.cargo_hologram.ui.DebugHud
import com.github.zomb_676.cargo_hologram.util.*
import com.mojang.brigadier.arguments.BoolArgumentType
import net.minecraftforge.registries.ForgeRegistries

object AllCommands : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher.registerCommand {
            dispatcher.registerCommand {
                CargoHologram.MOD_ID {
                    "global_settings" {
                        execute {
                            source.sendSystemMessage("mode:${GlobalFilter.globalListMode}".literal())
                            GlobalFilter.globalSelectors.forEach { selector ->
                                source.sendSystemMessage("type:${ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(selector.type)!!}".literal())
                                if (selector.slotSelectors.isNotEmpty()) {
                                    source.sendSystemMessage("selector:${selector.slotSelectors.joinToString(",")}".literal())
                                }
                            }
                            source.sendSystemMessage("allow lootChest:${GlobalFilter.allowLootChest}".literal())
                        }
                    }
                    "debug_overlay" {
                        "enable"(BoolArgumentType.bool()) {
                            execute {
                                val enable = this.getArgument<Boolean>("enable")
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
                            val enable = !DebugHud.enable
                            source.sendSystemMessage("DebugHud:$enable".literal())
                        }
                    }
                    "configure" {
                        execute {
                            val mc = currentMinecraft()
                            mc.tell {
                                mc.setScreen(ConfigureScreen())
                            }
                        }
                    }
                }
            }
        }
        dispatcher.registerClientCommand {
            CargoHologram.MOD_ID {
                "transform" {
                    execute {
                        TransformPlayerInvToNearbyPack(8).sendToServer()
                    }
                }
                "dumpAllMenuType" {
                    execute {
                        ForgeRegistries.MENU_TYPES.keys.forEach {entry ->
                            source.sendSystemMessage(entry.toString().literal())
                        }
                    }
                }
            }
        }
    }

}