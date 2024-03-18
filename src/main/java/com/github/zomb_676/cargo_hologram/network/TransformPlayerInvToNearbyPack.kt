package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.util.near
import com.github.zomb_676.cargo_hologram.util.toChunkPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.wrapper.InvWrapper
import net.minecraftforge.network.NetworkEvent

class TransformPlayerInvToNearbyPack(val distance: Int) : NetworkPack<TransformPlayerInvToNearbyPack> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): TransformPlayerInvToNearbyPack {
            val distance = buffer.readVarInt()
            return TransformPlayerInvToNearbyPack(distance)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(distance)
    }

    override fun handle(context: NetworkEvent.Context) {
        if (context.sender == null) return
        context.enqueueWork {
            val sender = context.sender ?: return@enqueueWork
            val playerPos = sender.blockPosition()
            val invWrapper = InvWrapper(sender.inventory)
            val level = sender.level()
            val s = playerPos.toChunkPos()
                .near(distance / 16)
                .filter { level.hasChunk(it.x,it.z) }
                .map { level.getChunk(it.x,it.z).blockEntities.entries }
                .flatten()
                .filter { it.value is CargoStorageBlockEntity }
                .filter { it.key.distSqr(playerPos) <= distance }
                .sortedBy { it.key.distSqr(playerPos) }
        }
    }

}