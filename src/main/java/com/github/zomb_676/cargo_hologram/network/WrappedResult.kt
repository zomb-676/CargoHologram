package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkEvent

class WrappedResult(
    val result: MonitorRawResult,
    val level: ResourceKey<Level>,
    private val chunkPos: ChunkPos,
) :
    NetworkPack<WrappedResult> {


    companion object {
        fun decode(buffer: FriendlyByteBuf): WrappedResult {
            val result = MonitorRawResult.decode(buffer)
            val level = buffer.readResourceKey(Registries.DIMENSION)
            val chunkPos = buffer.readChunkPos()
            return WrappedResult(result, level, chunkPos)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        result.encode(buffer)
        buffer.writeResourceKey(level)
        buffer.writeChunkPos(chunkPos)
    }

    override fun handle(context: NetworkEvent.Context) {
        ClientResultCache.cache(result, level, chunkPos)
    }

}