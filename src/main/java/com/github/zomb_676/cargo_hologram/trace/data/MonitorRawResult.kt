package com.github.zomb_676.cargo_hologram.trace.data

import com.github.zomb_676.cargo_hologram.network.WrappedResult
import com.github.zomb_676.cargo_hologram.util.MultiBlockContainerHandle
import com.github.zomb_676.cargo_hologram.util.SlotItemStack
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.ForgeCapabilities
import java.util.function.IntPredicate

class MonitorRawResult(val data: Map<BlockPos, List<SlotItemStack>>) :
    Iterable<Map.Entry<BlockPos, List<SlotItemStack>>> {

    class QueryRawResultBuilder {
        private val data: MutableMap<BlockPos, MutableList<SlotItemStack>> = mutableMapOf()

        fun build(): MonitorRawResult = MonitorRawResult(data)

        fun collectForBlockEntity(blockEntity: BlockEntity, slotCheck: IntPredicate, alreadySearched: IntAVLTreeSet) {
            val capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
            val items: MutableList<SlotItemStack> = mutableListOf()
            capability.ifPresent { itemHandle ->
                val identityHash = itemHandle.hashCode()
                if (identityHash in alreadySearched) return@ifPresent
                val maxSlots = itemHandle.slots
                for (querySlotIndex in 0..<maxSlots) {
                    if (slotCheck.test(querySlotIndex)) {
                        val stackInSlot = itemHandle.getStackInSlot(querySlotIndex)
                        if (stackInSlot.isEmpty) continue
                        items.add(SlotItemStack(querySlotIndex, stackInSlot.copy()))
                    }
                }
                if (items.isNotEmpty()) {
                    data[blockEntity.blockPos] = items
                }
                MultiBlockContainerHandle.handle(alreadySearched, itemHandle, identityHash, blockEntity)
            }
        }
    }

    companion object {
        fun beginBuild() = QueryRawResultBuilder()

        fun decode(buffer: FriendlyByteBuf): MonitorRawResult {
            val mapSize = buffer.readInt()
            val map: HashMap<BlockPos, List<SlotItemStack>> = HashMap(mapSize)
            repeat(mapSize) { _ ->
                val pos = buffer.readBlockPos()
                val items = List(buffer.readInt()) { _ ->
                    SlotItemStack.decode(buffer)
                }
                map[pos] = items
            }
            return MonitorRawResult(map)
        }
    }

    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeInt(data.size)
        data.forEach { (pos, items) ->
            buffer.writeBlockPos(pos)
            buffer.writeInt(items.size)
            items.forEach { i -> i.encode(buffer) }
        }
    }

    override operator fun iterator(): Iterator<Map.Entry<BlockPos, List<SlotItemStack>>> = data.iterator()

    fun isEmpty() = data.isEmpty()

    fun warpForPlayer(level: ServerLevel, chunkPos: ChunkPos) =
        WrappedResult(this, level.dimension(), chunkPos, MonitorType.PLAYER_CENTERED)
}