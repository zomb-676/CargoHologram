package com.github.zomb_676.cargo_hologram.trace.request

import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import com.github.zomb_676.cargo_hologram.trace.monitor.MonitorEntry
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.registries.ForgeRegistries
import org.apache.http.util.Asserts
import java.util.*
import java.util.function.IntPredicate

/**
 * request centered object
 */
sealed class QuerySource {
    private var attachedLevel: ResourceKey<Level> = Level.OVERWORLD
    private val attached: MutableMap<ChunkPos, MonitorEntry> = mutableMapOf()

    fun attachLevel(level: ResourceKey<Level>) {
        if (attachedLevel == level) return
        this.attachedLevel = level
    }

    fun attach(entry: MonitorEntry) {
        val before = attached.put(entry.chunkPos, entry)
        if (before != null)
            logOnDebug { error("attach chunk:${entry.chunkPos} while already attached") }
    }

    fun detach(chunkPos: ChunkPos) = attached.remove(chunkPos)
    fun detachAll() = attached.clear()
    fun attached() = attached.iterator()
    open fun queryBlockPosition() : Sequence<BlockPos> = emptySequence()

    /**
     * @param level the level of the data, not the source's level
     */
    abstract fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult)

    abstract fun valid(): Boolean
    abstract fun invalidate()
    open fun onRemove() {}

    fun filter(blockEntity: BlockEntity) = true
    open fun filterForBlockEntiry(blockEntity: BlockEntity): IntPredicate = GlobalFilter.ALWAYS_TRUE

    protected abstract fun requirement(): QueryRequirement
    fun force() = requirement().force
    fun crossDimension() = requirement().crossDimension
    fun selectors() = requirement().selector

    abstract fun fullChunk(): Boolean

    companion object {
        fun ofPlayerCentered(player: ServerPlayer, radius: Int, requirement: QueryRequirement): PlayerQuerySource {
            Asserts.check(radius > 0, "radius:$radius must > 0")
            Asserts.check(player.isOnline(), "player{name:${player.name},uuid:${player.uuid}}} is offline")
            return PlayerQuerySource(player.uuid, radius, requirement)
        }

        fun ofFixedPostion(
            player: ServerPlayer,
            loadChunk: Boolean,
            blockEntity: BlockEntity,
            requirement: QueryRequirement,
        ): PlayerFixedQuerySource {
            Asserts.check(player.isOnline(), "player{name:${player.name},uuid:${player.uuid}} is offline")
            return PlayerFixedQuerySource(player.uuid, loadChunk, blockEntity, requirement)
        }
    }

    class PlayerQuerySource(val player: UUID, val radius: Int, private val requirement: QueryRequirement) :
        QuerySource() {
        private var valid: Boolean = true
        override fun requirement(): QueryRequirement = requirement
        override fun fullChunk(): Boolean = true

        override fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult) {
            val player = player.queryPlayer() ?: throwOnDev() ?: return
            result.warpForPlayerCentered(level, chunkPos).sendToPlayer(player)
        }

        override fun invalidate() {
            this.valid = false
        }

        override fun valid(): Boolean = valid
    }

    class PlayerFixedQuerySource(
        val player: UUID,
        val loadChunk: Boolean,
        blockEntity: BlockEntity,
        private val requirement: QueryRequirement,
    ) : QuerySource() {
        private var valid = true
        val pos: BlockPos = blockEntity.blockPos
        val level: ResourceKey<Level> = blockEntity.level!!.dimension()
        val type: ResourceLocation = blockEntity.type.location(ForgeRegistries.BLOCK_ENTITY_TYPES)
        private val iter = listOf(pos)

        override fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult) {
            val player = player.queryPlayer() ?: throwOnDev() ?: return
            result.single(pos,level.dimension())?.sendToPlayer(player)
        }

        override fun invalidate() {
            this.valid = false
        }

        override fun queryBlockPosition(): Sequence<BlockPos> = iter.asSequence()
        override fun valid(): Boolean = valid
        override fun requirement(): QueryRequirement = requirement
        override fun fullChunk(): Boolean = false
    }
}