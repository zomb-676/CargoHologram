package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.ui.CargoStorageMenu
import com.github.zomb_676.cargo_hologram.util.log
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent

class SetPriorityPack(val pos: BlockPos, val newValue: Int) : NetworkPack<SetPriorityPack> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): SetPriorityPack {
            val pos = buffer.readBlockPos()
            val value = buffer.readInt()
            return SetPriorityPack(pos, value)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBlockPos(pos)
        buffer.writeInt(newValue)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            val sender = context.sender ?: return@enqueueWork
            when (val menu = sender.containerMenu) {
                is CargoStorageMenu -> {
                    menu.priorityData.set(newValue)
                }
                else -> log { debug("un-support menu:${menu::class.simpleName} for SetPriority") }
            }
        }
    }
}