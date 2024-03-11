package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.util.currentServer
import com.github.zomb_676.cargo_hologram.util.isOnline
import com.github.zomb_676.cargo_hologram.util.queryPlayer
import com.github.zomb_676.cargo_hologram.util.throwOnDev
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.network.PacketDistributor
import org.apache.http.util.Asserts
import java.util.*
import java.util.function.IntPredicate

/**
 * request centered object
 */
sealed class QuerySource {

    private val attachedChunk: Map<ResourceKey<Level>, MutableList<ChunkPos>> = ALL_LEVELS.associateWith { mutableListOf() }

    fun attachedChunks(): Map<ResourceKey<Level>, List<ChunkPos>> = attachedChunk

    /**
     * @param level the level of the data, not the source's level
     */
    abstract fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult)
    open fun valid(): Boolean = true
    fun onRemove() {}
    fun filter(blockEntity: BlockEntity) = true
    abstract fun requirement(): QueryRequirement
    open fun filterForBlockEntiry(blockEntity: BlockEntity): IntPredicate = ALWAYS_PASS_SLOT_FILTER

    fun attachChunk(level: ResourceKey<Level>, chunkPos: ChunkPos) = attachedChunk[level]!!.add(chunkPos)
    fun detachChunk(level: ResourceKey<Level>, chunkPos: ChunkPos) = attachedChunk[level]!!.remove(chunkPos)
    fun detachAllChunk(level: ResourceKey<Level>) = attachedChunk[level]!!.clear()
    fun iterAllChunks() = attachedChunk.iterator()
    fun iterAllChunks(level: ResourceKey<Level>): Iterator<ChunkPos> = attachedChunk[level]!!.iterator()
    abstract fun fullChunk(): Boolean

    companion object {
        private val ALL_LEVELS: Set<ResourceKey<Level>> = currentServer().levelKeys().toSet()
        val ALWAYS_PASS_SLOT_FILTER = IntPredicate { true }
        fun ofPlayerCentered(player: ServerPlayer, radius: Int, requirement: QueryRequirement): PlayerQuerySource {
            Asserts.check(radius > 0, "radius:$radius must > 0")
            Asserts.check(player.isOnline(), "player{name:${player.name},uuid:${player.uuid}}} is offline")
            return PlayerQuerySource(player.uuid, radius, requirement)
        }
    }

    class PlayerQuerySource(val player: UUID, val radius: Int, private val requirement: QueryRequirement) :
        QuerySource() {
        private var valid: Boolean = true
        override fun requirement(): QueryRequirement = requirement
        override fun fullChunk(): Boolean = true

        override fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult) {
            val player = player.queryPlayer() ?: throwOnDev() ?: return
            result.warpForPlayer(level, chunkPos).sendTo(PacketDistributor.PLAYER.with { player })
        }

        fun invalidate() {
            this.valid = false
        }

        override fun valid(): Boolean = valid
    }
}