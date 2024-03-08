package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.util.log
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler

class FilterMenu(containerId: Int, val playerInv: Inventory) :
    AbstractContainerMenu(AllRegisters.FILTER_MANU.get(), containerId) {

    val candidateHandle = ItemStackHandler(1)
    val candidateSlot: Slot = SlotItemHandler(candidateHandle, 0, 8, 8)
    val filterItem: ItemStack

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val slot = this.slots[pIndex]
        if (slot == candidateSlot) {
            slot.set(ItemStack.EMPTY)
            return ItemStack.EMPTY
        }
        val slotItem = slot.item
        if (slotItem.isEmpty) return ItemStack.EMPTY
        candidateSlot.set(slotItem)
        return ItemStack.EMPTY
    }

    override fun stillValid(pPlayer: Player): Boolean = true

    init {
        createInventorySlots(playerInv)
        addSlot(candidateSlot)
        filterItem = playerInv.player.mainHandItem
        if (filterItem.item != AllRegisters.Items.itemFilter.get())
            log { error("Filter Menu opened with Filter Item not in hand") }
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
        if (pSlot != candidateSlot) return false
        if (!pCarriedItem.isEmpty) {
            pSlot.set(pCarriedItem.copyWithCount(1))
        } else {
            pSlot.set(ItemStack.EMPTY)
        }
        return true
    }
}