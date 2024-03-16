package com.github.zomb_676.cargo_hologram.trace.request

import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
import com.github.zomb_676.cargo_hologram.trace.monitor.MonitorEntry
import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import com.github.zomb_676.cargo_hologram.util.*
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

    /**
     * @param level the level of the data, not the source's level
     */
    abstract fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult)

    open fun valid(): Boolean = true
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

        fun ofFixedPostion(player: UUID, loadChunk : Boolean, requirement: QueryRequirement) {

        }
    }

    class PlayerQuerySource(val player: UUID, val radius: Int, private val requirement: QueryRequirement) :
        QuerySource() {
        private var valid: Boolean = true
        override fun requirement(): QueryRequirement = requirement
        override fun fullChunk(): Boolean = true

        override fun send(level: ServerLevel, chunkPos: ChunkPos, result: MonitorRawResult) {
            val player = player.queryPlayer() ?: throwOnDev() ?: return
            result.warpForPlayer(level, chunkPos).sendToPlayer(player)
        }

        override fun invalidate() {
            this.valid = false
        }

        override fun valid(): Boolean = valid
    }

    class FixedPosition()
}