package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.currentClientPlayer
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.IEventBus

object ClientResultCache : BusSubscribe {
    private var playerCentered: MutableMap<ChunkPos, MonitorRawResult> = mutableMapOf()

    fun cache(result: MonitorRawResult, level: ResourceKey<Level>, chunkPos: ChunkPos, type: MonitorType) {
        when (type) {
            MonitorType.PLAYER_CENTERED -> {
                if (currentClientPlayer().level().dimension() != level) return
                if (result.isEmpty()) {
                    playerCentered.remove(chunkPos)
                } else {
                    playerCentered[chunkPos] = result
                }
            }
        }
    }

    fun cleanCache() {
        playerCentered.clear()
    }

    fun iterPlayerCentered(): Iterator<Map.Entry<ChunkPos, MonitorRawResult>> = playerCentered.iterator()

    override fun registerEvent(modBus: IEventBus, forgeBus: IEventBus) {
        forgeBus.addListener<PlayerEvent.PlayerChangedDimensionEvent> { event ->
            if (event.entity.level().isClientSide) {
                cleanCache()
            }
        }
        forgeBus.addListener<ClientPlayerNetworkEvent.LoggingOut> { event ->
            cleanCache()
        }

    }

    fun getPlayerCached(): Map<ChunkPos, MonitorRawResult> = playerCentered
}