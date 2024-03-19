package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.OpenBy
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraftforge.event.TickEvent

object ConsumeEnergyEventHandle : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<TickEvent.PlayerTickEvent> { event ->
            if ((event.phase != TickEvent.Phase.END)) return@dispatcher
            val menu = event.player.containerMenu as? CraftMenu? ?: return@dispatcher
            when (val openBy = menu.openBy) {
                is OpenBy.ByBlock -> TODO()
                is OpenBy.ByItem -> {
                    openBy.holdItem().getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent { cap ->
                        cap.current -= Config.Server.perOpenConsume
                    }
                }

                is OpenBy.ByMob -> throw RuntimeException()
            }
        }
    }

    fun takeConsume(extractItem: ItemStack,  player : ServerPlayer) {
        val consume = Config.Server.perItemTakeConsume * extractItem.count / extractItem.maxStackSize
        if (consume <= 0) return
        val menu = player.containerMenu
        if (menu is CraftMenu) {
            menu.openBy.onItem {
                holdItem().getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent {cap ->
                    cap.current -= consume
                }
            }
            menu.sendAllDataToRemote()
            return
        }
        val openItem = player.getItemInHand(InteractionHand.MAIN_HAND)
        openItem.getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent { cap ->
            cap.current -= consume
            val slot = player.inventory.selected
            player.connection.send(ClientboundContainerSetSlotPacket(-2, 0,slot , openItem))
        }
    }
}