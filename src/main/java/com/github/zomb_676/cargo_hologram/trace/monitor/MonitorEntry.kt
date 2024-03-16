package com.github.zomb_676.cargo_hologram.trace.monitor

import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import com.github.zomb_676.cargo_hologram.trace.request.QuerySource
import com.github.zomb_676.cargo_hologram.util.toChunkPos
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk

/**
 * search
 */
class MonitorEntry(val level: Level, val chunkPos: ChunkPos) {

    private val requirement: MonitorRequirement = MonitorRequirement(chunkPos)
    var result: MonitorRawResult? = null

    fun cleanResult() {
        result = null
    }

    /**
     * [result] update or not
     */
    fun tick(chunk: LevelChunk, alreadySearched: IntAVLTreeSet, haveTime: Boolean): Boolean {
        if (!(haveTime || requirement.force())) return false
        val builder = MonitorRawResult.beginBuild()
        if (requirement.fullChunk()) {
            chunk.blockEntities.values.asSequence()
        } else {
            requirement.blockPositioned().filter { pos -> pos.toChunkPos() == chunk.pos }
                .map { pos -> chunk.getBlockEntity(pos) }.filterNotNull()
        }.forEach { blockEntity: BlockEntity ->
            if (GlobalFilter.filterBlockEntity(blockEntity) && requirement.filterBlockEntity(blockEntity)) {
                builder.collectForBlockEntity(
                    blockEntity, requirement.slotFilterForBlockEntity(blockEntity), alreadySearched
                )
            }
        }
        result = builder.build()
        return true
    }

    /**
     * @param level the level of the data, not the source's level
     */
    fun send(level: ServerLevel, chunkPos: ChunkPos) {
        result?.also { res ->
            requirement.sendForAllSource(level, chunkPos, res)
        }
    }

    fun checkValid(): Boolean = requirement.checkAndInvalidateSource()

    fun addSource(source: QuerySource) = requirement.addSource(source)

    fun removeSource(source: QuerySource) = requirement.removeSource(source)
}