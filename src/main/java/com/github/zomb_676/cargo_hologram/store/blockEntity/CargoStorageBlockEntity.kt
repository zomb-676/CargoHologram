package com.github.zomb_676.cargo_hologram.store.blockEntity

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.AllRegisters.BlockEntities.cargoStorage
import com.github.zomb_676.cargo_hologram.util.asItemStack
import com.github.zomb_676.cargo_hologram.util.filter.TraitList
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.Containers
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.ItemStackHandler

class CargoStorageBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(cargoStorage.get(), pos, state) {

    companion object {
        const val HANDLE_NBT_KEY = "store"
        const val PRIORITY_NBT_KEY = "priority"
        const val TRAIT_NBT_KEY = "traits"
        const val DISPLAY_ITEM_NBT_KEY = "display_item"
    }

    var filterItem : ItemStack = AllRegisters.Items.traitFilter.get().asItemStack()
        set(value) {
            if (value.isEmpty) {
                field = AllRegisters.Items.traitFilter.get().asItemStack()
                traitList = TraitList()
                return
            }
            if (!value.`is`(AllRegisters.Items.traitFilter.get())) return
            field = value
            traitList = TraitList()
            traitList.readFromItem(value)
        }
    var traitList = TraitList()
    var priority: Int = 0
    val handle = ItemStackHandler(36)
    var displayItem = ItemStack.EMPTY

    override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap != ForgeCapabilities.ITEM_HANDLER) return LazyOptional.empty()
        return LazyOptional.of { handle }.cast()
    }

    override fun saveAdditional(pTag: CompoundTag) {
        super.saveAdditional(pTag)
        pTag.put(HANDLE_NBT_KEY, handle.serializeNBT())
        pTag.putInt(PRIORITY_NBT_KEY, priority)
        pTag.put(TRAIT_NBT_KEY, traitList.serializeNBT())
        pTag.put(DISPLAY_ITEM_NBT_KEY, displayItem.serializeNBT())
    }

    override fun load(pTag: CompoundTag) {
        super.load(pTag)
        handle.deserializeNBT(pTag.getCompound(HANDLE_NBT_KEY))
        priority = pTag.getInt(PRIORITY_NBT_KEY)
        traitList.deserializeNBT(pTag.getCompound(TRAIT_NBT_KEY))
        traitList.writeToItem(filterItem)
        displayItem = ItemStack.of(pTag.getCompound(DISPLAY_ITEM_NBT_KEY))
    }

    fun dropContent() {
        val x = blockPos.x.toDouble()
        val y = blockPos.y.toDouble()
        val z = blockPos.z.toDouble()
        val level = level ?: return
        repeat(handle.slots) { slotIndex ->
            val itemStack = handle.getStackInSlot(slotIndex)
            if (itemStack.isEmpty) return@repeat
            Containers.dropItemStack(level, x, y, z, itemStack)
        }
    }

    override fun getUpdateTag(): CompoundTag {
        val tag = super.getUpdateTag()
        tag.putInt(PRIORITY_NBT_KEY, priority)
        tag.put(TRAIT_NBT_KEY, traitList.serializeNBT())
        tag.put(DISPLAY_ITEM_NBT_KEY, displayItem.serializeNBT())
        return tag
    }

    override fun handleUpdateTag(tag: CompoundTag) {
        priority = tag.getInt(PRIORITY_NBT_KEY)
        traitList.deserializeNBT(tag.getCompound(TRAIT_NBT_KEY))
        traitList.writeToItem(filterItem)
        displayItem = ItemStack.of(tag.getCompound(DISPLAY_ITEM_NBT_KEY))
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun onDataPacket(net: Connection, pkt: ClientboundBlockEntityDataPacket) {
        val tag = pkt.tag ?: return
        super.load(tag)
        handleUpdateTag(tag)
    }

    fun sendClientUpdate() {
        level?.sendBlockUpdated(blockPos,blockState,blockState, Block.UPDATE_CLIENTS)
    }
}