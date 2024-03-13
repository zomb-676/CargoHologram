package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler

class CargoStorageMenu(containerId: Int, val playerInv: Inventory, pos: BlockPos) :
    AbstractContainerMenu(AllRegisters.Menus.CARGO_STORAGE_MENU.get(), containerId) {
    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val item = this.slots[pIndex].item
        if (item.`is`(AllRegisters.Items.traitFilter.get())) {
            filterItemSlot.set(item.copy())
        }
        return ItemStack.EMPTY
    }

    override fun stillValid(pPlayer: Player): Boolean = true

    val storageHandle : ItemStackHandler
    val storageSlots: List<SlotItemHandler>
    val displayItemContainer = ItemStackHandler(1)
    val filterItemContainer = ItemStackHandler(1)
    val displayItemSlot: SlotItemHandler
    val filterItemSlot : SlotItemHandler

    init {
        createInventorySlots(playerInv)
        val blockEntity =
            playerInv.player.level().getBlockEntity(pos) as CargoStorageBlockEntity? ?: throw RuntimeException()
        storageHandle = blockEntity.handle
        storageSlots = List(storageHandle.slots) { index ->
            val x = index % 9
            val y = index / 9
            SlotItemHandler(storageHandle, index, 4 + x * 19 + 1, y * 19 + 2)
        }
        storageSlots.forEach(::addSlot)
        displayItemSlot = object : SlotItemHandler(displayItemContainer, 0, -1 * 19, 2) {
            override fun set(stack: ItemStack) {
                blockEntity.displayItem = stack
                blockEntity.setChanged()
            }
            override fun getItem(): ItemStack = blockEntity.displayItem
        }
        this.addSlot(displayItemSlot)
        filterItemSlot = object : SlotItemHandler(filterItemContainer, 0, -1 * 19, 2 + 19) {
            override fun set(stack: ItemStack) {
                blockEntity.filterItem = stack
                blockEntity.setChanged()
            }
            override fun getItem(): ItemStack = blockEntity.filterItem
        }
        this.addSlot(filterItemSlot)
    }

    private fun createInventorySlots(pInventory: Inventory) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(pInventory, j + i * 9 + 9, 4 + j * 19, 136 + i * 19))
            }
        }

        for (k in 0..8) {
            this.addSlot(Slot(pInventory, k, 4 + k * 19, 195))
        }
    }

    override fun tryItemClickBehaviourOverride(
        pPlayer: Player,
        pAction: ClickAction,
        pSlot: Slot,
        pClickedItem: ItemStack,
        pCarriedItem: ItemStack,
    ): Boolean {
        if (pSlot == displayItemSlot) {
            pSlot.set(pCarriedItem.copyWithCount(1))
            return true
        } else if (pSlot == filterItemSlot) {
            if (pCarriedItem.`is`(AllRegisters.Items.traitFilter.get())) {
                pSlot.set(pCarriedItem.copy())
            } else {
                pSlot.set(ItemStack.EMPTY)
            }
            return true
        }
        return false
    }
}