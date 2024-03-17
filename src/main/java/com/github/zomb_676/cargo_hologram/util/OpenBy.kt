package com.github.zomb_676.cargo_hologram.util

import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import java.util.*
import javax.annotation.OverridingMethodsMustInvokeSuper

sealed class OpenBy {

    lateinit var player: Player
        private set
    lateinit var level: Level
        private set

    inline fun <reified T : OpenBy> expect(): T {
        if (this is T) {
            return this
        } else throw AssertionError("expected ${this::class.simpleName} as ${T::class.simpleName}")
    }

    inline fun onBlock(code: (ByBlock).() -> Unit): OpenBy {
        if (this is ByBlock) code(this)
        return this
    }

    inline fun onItem(code: ByItem.() -> Unit): OpenBy {
        if (this is ByItem) code(this)
        return this
    }

    inline fun onMob(code: ByMob.() -> Unit): OpenBy {
        if (this is ByMob) code(this)
        return this
    }

    abstract fun write(buffer: FriendlyByteBuf)

    @OverridingMethodsMustInvokeSuper
    open fun onMenuInit(inv: Inventory) {
        this.player = inv.player
        this.level = player.level()
    }

    companion object {
        fun read(buffer: FriendlyByteBuf): OpenBy {
            return when (buffer.readByte()) {
                ByItem.TYPE -> ByItem.read(buffer)
                ByBlock.TYPE -> ByBlock.read(buffer)
                ByMob.TYPE -> ByMob.read(buffer)
                else -> throw RuntimeException()
            }
        }

        private fun getCurrentHot() = currentClientPlayer().inventory.selected

        fun byBlock(pos: BlockPos, blockState: BlockState, level: ResourceKey<Level>) = ByBlock(pos, blockState, level)

        fun byBlock(blockEntity: BlockEntity) =
            ByBlock(blockEntity.blockPos, blockEntity.blockState, blockEntity.level!!.dimension())

        fun byItem(item: ItemStack, slot: Int = getCurrentHot()) = ByItem(item, slot)
    }

    class ByItem(val item: ItemStack, val slot: Int) : OpenBy() {
        companion object {
            const val TYPE: Byte = 0
            fun read(buffer: FriendlyByteBuf): ByItem {
                val item = buffer.readItem()
                val slot = buffer.readVarInt()
                return ByItem(item, slot)
            }
        }

        override fun write(buffer: FriendlyByteBuf) {
            buffer.writeByte(TYPE.toInt())
            buffer.writeItem(item)
            buffer.writeVarInt(slot)
        }
    }

    class ByBlock(val pos: BlockPos, val state: BlockState, val levelKey: ResourceKey<Level>) : OpenBy() {
        var blockEntity: BlockEntity? = null

        companion object {
            const val TYPE: Byte = 1
            fun read(buffer: FriendlyByteBuf): ByBlock {
                val pos = buffer.readBlockPos()
                val state = Block.stateById(buffer.readVarInt())
                val level = buffer.readResourceKey(Registries.DIMENSION)
                return ByBlock(pos, state, level)
            }
        }

        override fun write(buffer: FriendlyByteBuf) {
            buffer.writeByte(TYPE.toInt())
            buffer.writeBlockPos(pos)
            buffer.writeVarInt(Block.getId(state))
            buffer.writeResourceKey(levelKey)
        }

        fun distance(player: Player, distance: Int = 10): Boolean {
            if (player.position()
                    .distanceToSqr(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()) > distance
            ) return false
            val level = player.level()
            if (level.dimension() != level) return false
            if (level.getBlockState(pos) != state) return false
            return true
        }

        inline fun <reified T : BlockEntity> expect(): T = when (val be = blockEntity) {
            is T -> be
            null -> throw AssertionError("$levelKey at pos:$pos have not a blockEntity")
            else -> throw AssertionError("expect:${T::class.simpleName}, actual ${be::class.simpleName}")
        }

        override fun onMenuInit(inv: Inventory) {
            super.onMenuInit(inv)
            blockEntity = level.getBlockEntity(pos)
        }
    }

    class ByMob(val uuid: UUID, val levelKey: ResourceKey<Level>) : OpenBy() {
        companion object {
            const val TYPE: Byte = 2
            fun read(buffer: FriendlyByteBuf): ByMob {
                val uuid = buffer.readUUID()
                val level = buffer.readResourceKey(Registries.DIMENSION)
                return ByMob(uuid, level)
            }
        }

        override fun write(buffer: FriendlyByteBuf) {
            buffer.writeByte(TYPE.toInt())
            buffer.writeUUID(uuid)
            buffer.writeResourceKey(levelKey)
        }
    }
}