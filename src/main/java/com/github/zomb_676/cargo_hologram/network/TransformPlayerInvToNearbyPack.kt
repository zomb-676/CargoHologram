package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.util.FavouriteItemUtils
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
            val blockEntities = playerPos.toChunkPos()
                .near((distance / 16) + 1)
                .filter { level.hasChunk(it.x, it.z) }
                .map { level.getChunk(it.x, it.z).blockEntities.entries }
                .flatten()
                .filter { it.value.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent }
                .filter { it.key.distSqr(playerPos) <= (distance * distance).toDouble() }
                .sortedBy { it.key.distSqr(playerPos) }
                .map { it.value as? CargoStorageBlockEntity? }
                .filterNotNull()
                .toList()
            inv@ for (invIndex in 0..<invWrapper.slots) {
                var transItem = invWrapper.getStackInSlot(invIndex)
                if (transItem.isEmpty) continue@inv
                if (FavouriteItemUtils.isFavourite(transItem)) continue@inv
                transItem = transItem.copy()
                val handle = blockEntities.firstOrNull { it.traitList.test(transItem) }?.handle ?: continue@inv
                trans@ for (containerSlot in 0..<handle.slots) {
                    transItem = handle.insertItem(containerSlot, transItem, false)
                    if (transItem.isEmpty) break@trans
                }
                invWrapper.setStackInSlot(invIndex, transItem)
            }
        }
    }

}