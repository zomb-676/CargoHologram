package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.MonitorRawResult
import com.github.zomb_676.cargo_hologram.trace.MonitorType
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkEvent

class WrappedResult(
    val result: MonitorRawResult,
    val level: ResourceKey<Level>,
    val chunkPos: ChunkPos,
    val type: MonitorType,
) :
    NetworkPack<WrappedResult> {


    companion object {
        fun decode(buffer: FriendlyByteBuf): WrappedResult {
            val result = MonitorRawResult.decode(buffer)
            val level = buffer.readResourceKey(Registries.DIMENSION)
            val chunkPos = buffer.readChunkPos()
            val type = buffer.readEnum(MonitorType::class.java)
            return WrappedResult(result, level, chunkPos, type)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        result.encode(buffer)
        buffer.writeResourceKey(level)
        buffer.writeChunkPos(chunkPos)
        buffer.writeEnum(type)
    }

    override fun handle(context: NetworkEvent.Context) {
        ClientResultCache.cache(result, level, chunkPos, type)
    }

}