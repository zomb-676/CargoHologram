package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler

class FilterMenu(containerId: Int, val playerInv: Inventory) :
    AbstractContainerMenu(AllRegisters.Menus.filterManu.get(), containerId) {

    val candidateHandle = ItemStackHandler(1)
    val candidateSlot: Slot = SlotItemHandler(candidateHandle, 0, 8, 8)

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val slot = this.slots[pIndex]
        if (slot == candidateSlot) {
            slot.set(ItemStack.EMPTY)
            return ItemStack.EMPTY
        }
        val slotItem = slot.item
        if (slotItem.isEmpty) return ItemStack.EMPTY
        candidateSlot.set(slotItem.copyWithCount(1))
        return ItemStack.EMPTY
    }

    init {
        createInventorySlots(playerInv)
        addSlot(candidateSlot)
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

    override fun clicked(pSlotId: Int, pButton: Int, pClickType: ClickType, pPlayer: Player) {
        if (pSlotId == playerInv.selected + 27 && pClickType != ClickType.THROW) return
        super.clicked(pSlotId, pButton, pClickType, pPlayer)
    }

    override fun stillValid(pPlayer: Player): Boolean = playerInv.getSelected()
        .item == (AllRegisters.Items.traitFilter.get())
}