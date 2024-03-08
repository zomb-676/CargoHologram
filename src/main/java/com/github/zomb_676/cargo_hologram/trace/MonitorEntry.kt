package com.github.zomb_676.cargo_hologram.trace

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk

/**
 * search
 */
class MonitorEntry {

    private val requirement: MonitorRequirement = MonitorRequirement()
    private var result: MonitorRawResult? = null

    fun cleanResult() {
        result = null
    }

    /**
     * [result] update or not
     */
    fun tick(chunk: LevelChunk, alreadySearched: IntAVLTreeSet, haveTime: Boolean): Boolean {
        if (!(haveTime || requirement.force())) return false
        val builder = MonitorRawResult.beginBuild()
        chunk.blockEntities.forEach { (_, blockEntity: BlockEntity) ->
            if (GlobalFilter.filterBlockEntity(blockEntity) && requirement.filterBlockEntity(blockEntity)) {
                builder.collectForBlockEntity(
                    blockEntity,
                    requirement.slotFilterForBlockEntity(blockEntity), alreadySearched
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