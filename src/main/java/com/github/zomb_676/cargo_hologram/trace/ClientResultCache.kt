package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import com.github.zomb_676.cargo_hologram.trace.data.ResultPack
import com.github.zomb_676.cargo_hologram.trace.data.SingleRawResult
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraftforge.client.event.ClientPlayerNetworkEvent
import net.minecraftforge.event.entity.player.PlayerEvent
import org.apache.http.util.Asserts
import kotlin.math.abs
import kotlin.math.max

object ClientResultCache : BusSubscribe {
    var playerCentered: MutableMap<ChunkPos, ProcessedResult> = mutableMapOf()
    var fixedPosition: MutableMap<ResourceKey<Level>, MutableMap<BlockPos, ProcessedResult>> = mutableMapOf()
    var fixedTraced: MutableMap<ResourceKey<Level>, MutableList<BlockPos>> = mutableMapOf()

    fun cache(result: ResultPack, level: ResourceKey<Level>, chunkPos: ChunkPos) {
        when (result) {
            is MonitorRawResult -> {
                if (currentClientPlayer().level().dimension() != level) return
                if (result.isEmpty()) {
                    playerCentered.remove(chunkPos)
                } else {
                    playerCentered[chunkPos] = ProcessedResult.convert(result)
                }
            }

            is SingleRawResult -> {
                val map = fixedPosition.computeIfAbsent(level) { mutableMapOf() }

            }
        }
    }

    fun cleanPlayerCenteredCache() {
        playerCentered.clear()
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<PlayerEvent.PlayerChangedDimensionEvent> { event ->
            if (event.entity.level().isClientSide) {
                cleanPlayerCenteredCache()
            }
        }
        dispatcher<ClientPlayerNetworkEvent.LoggingOut> { event ->
            cleanPlayerCenteredCache()
            fixedPosition.clear()
            fixedTraced.clear()
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

    inline fun iter(str: String, f: (pos: BlockPos, block: Block, slot: Int, item: ItemStack) -> Unit): Int {
        val backed = SearchEngine.getBacked()
        backed.searchText = str
        var count = 0
        playerCentered.forEach { (_, res) ->
            val s = if (str.isEmpty()) res.iterBy { true } else res.iterBy(backed::containsInResult)
            s.forEach { (pos, seq) ->
                val block = currentMinecraft().level!!.getBlockState(pos).block
                seq.forEach { (slot, item) ->
                    f(pos, block, slot, item)
                }
            }
            count += s.getCount()
        }
        return count
    }
}