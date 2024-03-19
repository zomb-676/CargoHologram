package com.github.zomb_676.cargo_hologram.blockEntity

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.item.LinkerItem
import com.github.zomb_676.cargo_hologram.util.location
import com.github.zomb_676.cargo_hologram.util.query
import com.github.zomb_676.cargo_hologram.util.toBlockPos
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.ForgeRegistries

class InserterBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(AllRegisters.BlockEntities.inserter.get(), pos, state) {

    companion object {
        private const val LIST_TAG_NAME = "linked"
        private const val LINKED_TYPE_TAG_NAME = "type"
        private const val LINKED_POS_TAG_NAME = "pos"
    }

    fun setLinked(item: ItemStack) {
        linked = LinkerItem.LinkData.linked(item)
        this.setChanged()
    }

    var linked: List<Pair<BlockEntityType<*>, BlockPos>> = emptyList()
        set(value) {
            field = value
            (level ?: return).apply {
                sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
            }
        }

    override fun saveAdditional(pTag: CompoundTag) {
        super.saveAdditional(pTag)
        saveLinked(pTag)
    }

    override fun load(pTag: CompoundTag) {
        super.load(pTag)
        if (!pTag.contains(LIST_TAG_NAME, Tag.TAG_LIST.toInt())) return
        loadLinked(pTag)
    }

    override fun getUpdateTag(): CompoundTag =
        CompoundTag().apply(::saveLinked)

    override fun handleUpdateTag(tag: CompoundTag) {
        super.handleUpdateTag(tag)
        loadLinked(tag)
    }

    private fun loadLinked(pTag: CompoundTag) {
        linked = pTag.getList(LIST_TAG_NAME, Tag.TAG_COMPOUND.toInt()).map {
            (it as CompoundTag).run {
                ResourceLocation(getString(LINKED_TYPE_TAG_NAME)).query(ForgeRegistries.BLOCK_ENTITY_TYPES) to
                        getLong(LINKED_POS_TAG_NAME).toBlockPos()
            }
        }
    }

    private fun saveLinked(pTag: CompoundTag) {
        val listTag = ListTag()
        linked.forEach { (type, pos) ->
            val compound = CompoundTag()
            compound.putString(LINKED_TYPE_TAG_NAME, type.location(ForgeRegistries.BLOCK_ENTITY_TYPES).toString())
            compound.putLong(LINKED_POS_TAG_NAME, pos.asLong())
            listTag.add(compound)
        }
        pTag.put(LIST_TAG_NAME, listTag)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

}