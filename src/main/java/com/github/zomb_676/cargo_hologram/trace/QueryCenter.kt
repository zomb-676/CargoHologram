package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.trace.monitor.MonitorCenter
import com.github.zomb_676.cargo_hologram.trace.request.QuerySource
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import java.util.*
import java.util.function.Function
import java.util.stream.Stream

/**
 * work between [QuerySource] and [MonitorCenter]
 */
object QueryCenter : BusSubscribe {
    val playerSources: MutableMap<UUID, QuerySource.PlayerQuerySource> = mutableMapOf()
    private val playerTrace: MutableMap<UUID, LastPlayerLocation> = mutableMapOf()
    private val fixedTrace: MutableMap<ResourceKey<Level>, MutableMap<BlockPos, MutableSet<QuerySource.PlayerFixedQuerySource>>> =
        mutableMapOf()

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
            val currentX = currentPos.sectionX()
            val currentZ = currentPos.sectionZ()
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
                monitorForPlayer(source.player.queryPlayer()!!, source)
            }

            is QuerySource.PlayerFixedQuerySource -> {
                fixedTrace.computeIfAbsent(source.level) { mutableMapOf() }
                    .computeIfAbsent(source.pos) { mutableSetOf() }
                    .add(source)
                monitorForFixedPosition(source)
            }
        }
    }

    fun stopPlayer(playerUuid: UUID) {
        playerSources.remove(playerUuid)!!.invalidate()
        playerTrace.remove(playerUuid)
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<ServerTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@dispatcher
            playerSources.forEach { (playerUuid, source) ->
                val player = playerUuid.queryPlayer() ?: return@forEach
                if (playerTrace[playerUuid]!!.update(player)) {
                    source.attached().forEach { (_, entry) -> entry.removeSource(source) }
                    source.detachAll()
                    source.attachLevel(player.level().dimension())
                    monitorForPlayer(player, source)
                }
            }
        }
        dispatcher<PlayerEvent.PlayerLoggedOutEvent> { event ->
            val playerUuid = event.entity.uuid
            playerSources.remove(playerUuid)?.invalidate()
            playerTrace.remove(playerUuid)
        }
    }

    private fun monitorForPlayer(
        player: ServerPlayer,
        source: QuerySource.PlayerQuerySource,
    ) {
        val level = player.level()
        source.attachLevel(player.level().dimension())
        ChunkPos(player.blockPosition()).near(source.radius) { chunkPos ->
            MonitorCenter.monitor(level, chunkPos, source)
        }
    }

    private fun monitorForFixedPosition(source: QuerySource.PlayerFixedQuerySource) {
        source.attachLevel(source.level)
        MonitorCenter.monitor(source.level.dim(), source.pos.toChunkPos(), source)
    }

}