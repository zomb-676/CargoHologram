package com.github.zomb_676.cargo_hologram.trace

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.IntPredicate

/**
 *  one [MonitorRequirement] pair to one chunk in a level and multi [QuerySource]
 */
class MonitorRequirement(val chunkPos: ChunkPos) {

    private val sources: MutableList<QuerySource> = mutableListOf()

    private var forceCount: Int = 0
    private var fullChunkCount : Int = 0
    private val map: MutableMap<BlockEntityType<*>, IntPredicate> = mutableMapOf()

    fun force(): Boolean = forceCount > 0
    fun fullChunk() : Boolean = fullChunkCount > 0

    fun filterBlockEntity(blockEntity: BlockEntity): Boolean = sources.any { it.filter(blockEntity) }

    fun slotFilterForBlockEntity(blockEntity: BlockEntity): IntPredicate = map[blockEntity.type] ?: QuerySource.ALWAYS_PASS_SLOT_FILTER

    /**
     * @param level the level of the data, not the source's level
     */
    fun sendForAllSource(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult) {
        sources.forEach { source -> source.send(level, chunkPos, result) }
    }

    fun addSource(querySource: QuerySource) {
        sources.add(querySource)
        val requirement = querySource.requirement()
        requirement.selector.forEach { selector ->
//            val before = map.put(selector.type, selector)
            //TODO
        }
        if (requirement.force) {
            forceCount++
        }
    }

    fun removeSource(querySource: QuerySource) {
        if (sources.remove(querySource)) {
            val requirement = querySource.requirement()
            if (requirement.force) {
                forceCount--
                //TODO
            }
        }
    }

    /**
     * @return if false, this requirement should be removed
     */
    fun checkAndInvalidateSource(): Boolean {
        val iter = sources.iterator()
        while (iter.hasNext()) {
            val source = iter.next()
            if (source.valid()) continue
            source.onRemove()
            if (source.requirement().force) this.forceCount--
            if (source.fullChunk()) this.fullChunkCount--
        }
        return sources.isNotEmpty()
    }


}