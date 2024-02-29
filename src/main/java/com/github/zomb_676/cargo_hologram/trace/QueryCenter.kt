package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.log
import com.github.zomb_676.cargo_hologram.util.near
import com.github.zomb_676.cargo_hologram.util.queryPlayer
import net.minecraft.core.SectionPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.eventbus.api.IEventBus
import java.util.*

/**
 * work between [QuerySource] and [MonitorCenter]
 */
object QueryCenter : BusSubscribe {
    private val playerSources: MutableMap<UUID, QuerySource.PlayerQuerySource> = mutableMapOf()
    private val playerTrace: MutableMap<UUID, LastPlayerLocation> = mutableMapOf()

    class LastPlayerLocation(player: ServerPlayer) {
        private var lastLevel: ResourceKey<Level> = player.level().dimension()
        private var lastChunkPos: ChunkPos = ChunkPos(player.blockPosition())

        /**
         * @return if player change chunk or level
         */
        fun update(player: ServerPlayer): Boolean {
            var change = false
            val currentLevel = player.level().dimension()
            if (currentLevel != lastLevel) {
                lastLevel = currentLevel
                change = true
            }
            val currentPos = player.blockPosition()
            val currentX = SectionPos.blockToSectionCoord(currentPos.x)
            val currentZ = SectionPos.blockToSectionCoord(currentPos.z)
            if (currentX != lastChunkPos.x || currentZ != lastChunkPos.z) {
                lastChunkPos = ChunkPos(currentX, currentZ)
                change = true
            }

            return change
        }
    }

    fun appendSource(source: QuerySource) {
        when (source) {
            is QuerySource.PlayerQuerySource -> {
                playerSources.put(source.player, source)?.invalidate()
                playerTrace[source.player] = LastPlayerLocation(source.player.queryPlayer()!!)
                //TODO
            }
        }
    }

    fun stopPlayer(playerUuid: UUID) {
        playerSources.remove(playerUuid)!!.invalidate()
    }

    override fun registerEvent(modBus: IEventBus, forgeBus: IEventBus) {
        forgeBus.addListener<ServerTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@addListener
            playerSources.forEach { (playerUuid, source) ->
                val player = playerUuid.queryPlayer() ?: return@forEach
                if (playerTrace[playerUuid]!!.update(player)) {
                    log { debug("update monitor for player:{}", player) }
                    val level = player.level()
                    ChunkPos(player.blockPosition()).near(source.radius) { chunkPos ->
                        MonitorCenter.monitor(level, chunkPos, source)
                    }
                }
            }
        }
        forgeBus.addListener<PlayerEvent.PlayerLoggedOutEvent> { event ->
            val playerUuid = event.entity.uuid
            playerSources.remove(playerUuid)
            playerTrace.remove(playerUuid)
        }
    }



}