package com.github.zomb_676.cargo_hologram.trace.data

import com.github.zomb_676.cargo_hologram.network.NetworkPack
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.util.SlotItemStack
import com.github.zomb_676.cargo_hologram.util.toChunkPos
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkEvent

class SingleRawResult(val pos: BlockPos, val level: ResourceKey<Level>, val data: List<SlotItemStack>) :
    NetworkPack<SingleRawResult>, ResultPack {

    companion object {
        fun decode(buffer: FriendlyByteBuf): SingleRawResult {
            val pos = buffer.readBlockPos()
            val level = buffer.readResourceKey(Registries.DIMENSION)
            val data = List(buffer.readInt()) { _ ->
                val slot = buffer.readVarInt()
                val item = buffer.readItem()
                SlotItemStack(slot, item)
            }
            return SingleRawResult(pos, level, data)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBlockPos(pos)
        buffer.writeResourceKey(level)
        buffer.writeInt(data.size)
        data.forEach { (slot, item) ->
            buffer.writeVarInt(slot)
            buffer.writeItemStack(item, false)
        }
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            ClientResultCache.cache(this,level, pos.toChunkPos())
        }
    }

}