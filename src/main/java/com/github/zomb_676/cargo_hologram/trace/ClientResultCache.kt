package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import org.apache.http.util.Asserts
import kotlin.math.abs
import kotlin.math.max

object ClientResultCache : BusSubscribe {
    private var playerCentered: MutableMap<ChunkPos, ProcessedResult> = mutableMapOf()

    fun cache(result: MonitorRawResult, level: ResourceKey<Level>, chunkPos: ChunkPos, type: MonitorType) {
        when (type) {
            MonitorType.PLAYER_CENTERED -> {
                if (currentClientPlayer().level().dimension() != level) return
                if (result.isEmpty()) {
                    playerCentered.remove(chunkPos)
                } else {
                    playerCentered[chunkPos] = ProcessedResult.convert(result)
                }
            }
        }
    }

    fun cleanCache() {
        playerCentered.clear()
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<PlayerEvent.PlayerChangedDimensionEvent> { event ->
            if (event.entity.level().isClientSide) {
                cleanCache()
            }
        }
        dispatcher<ClientPlayerNetworkEvent.LoggingOut> { event ->
            cleanCache()
        }
    }

    fun isEmpty() = playerCentered.isEmpty()

    fun iter(distance: Int = -1): Iterator<Map.Entry<ChunkPos, ProcessedResult>> {
        Asserts.check(distance >= -1, "distance must > 0")
        if (distance == -1) return playerCentered.iterator()
        if (distance == 0) {
            val chunkPos = currentClientPlayer().blockPosition().toChunkPos()
            val entry = playerCentered[chunkPos] ?: return emptyMap<ChunkPos, ProcessedResult>().iterator()
            return mapOf(chunkPos to entry).iterator()
        }
        val playerPos = currentClientPlayer().blockPosition()
        val playerX = playerPos.sectionX()
        val playerZ = playerPos.sectionZ()
        return playerCentered.asSequence().filter { (pos, _) ->
            max(abs(playerX - pos.x), abs(playerZ - pos.z)) <= distance
        }.iterator()
    }

    fun count(): Int = playerCentered.values.sumOf(ProcessedResult::count)
}