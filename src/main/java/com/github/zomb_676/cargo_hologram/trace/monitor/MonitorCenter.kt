package com.github.zomb_676.cargo_hologram.trace.monitor

import com.github.zomb_676.cargo_hologram.trace.request.QuerySource
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.runOnDistClient
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.LevelTickEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.event.level.LevelEvent as ForgeLevelEvent

/**
 * result based object, for scan and send to client
 */
object MonitorCenter : BusSubscribe {

    val queryMap: MutableMap<ResourceKey<Level>, MutableMap<ChunkPos, MonitorEntry>> = mutableMapOf()

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<ForgeLevelEvent.Unload> { event ->
            val level: LevelAccessor = event.level
            if (level is ServerLevel) {
                val dimKey: ResourceKey<Level> = level.dimension()
                queryMap[dimKey]!!.forEach { it.value.cleanResult() }
            }
        }
        dispatcher<ForgeLevelEvent.Load> { event ->
            val level: LevelAccessor = event.level
            if (level is ServerLevel) {
                val dimKey: ResourceKey<Level> = level.dimension()
                queryMap.computeIfAbsent(dimKey) { _ -> mutableMapOf() }
            }
        }
        runOnDistClient {
            {
                dispatcher<ClientPlayerNetworkEvent.LoggingOut> { _ ->
                    queryMap.forEach { (_, v) -> v.clear() }
                }
            }
        }
        dispatcher<LevelTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@dispatcher
            if (event.side != LogicalSide.SERVER) return@dispatcher
            val haveTime = event.haveTime() or true
            val level = event.level as ServerLevel
            val iter = queryMap[level.dimension()]!!.iterator()
            val alreadySearched = IntAVLTreeSet()
            while (iter.hasNext()) {
                val (pos, entry) = iter.next()
                if (entry.checkValid()) {
                    if (level.hasChunk(pos.x, pos.z)) {
                        if (entry.tick(level.getChunk(pos.x, pos.z), alreadySearched, haveTime)) {
                            entry.send(level, pos)
                        }
                    }
                } else {
                    iter.remove()
                }
            }
        }
    }

    fun monitor(level: Level, chunkPos: ChunkPos, source: QuerySource) {
        val map = queryMap[level.dimension()]!!
        val entry = map.computeIfAbsent(chunkPos) { MonitorEntry(level, chunkPos) }
        entry.addSource(source)
        source.attach(entry)
    }

    fun stopMonitor(level: ResourceKey<Level>, source: QuerySource) {
        source.attached().forEach { (_, entry) ->
            entry.removeSource(source)
        }
        source.detachAll()
    }
}