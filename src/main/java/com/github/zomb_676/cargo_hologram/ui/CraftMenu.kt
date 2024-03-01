package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler

class CraftMenu(containerId: Int, playerInv: Inventory) :
    AbstractContainerMenu(AllRegisters.CRAFTER_MANU.get(), containerId) {

    val mateiralHandle = ItemStackHandler(9)
    val resultHandle = ItemStackHandler(1)
    val craft = Array(9) { index ->
        SlotItemHandler(mateiralHandle, index, index % 3, index / 3)
    }
    val result = SlotItemHandler(resultHandle,0, 3,1)


    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(pPlayer: Player): Boolean = true

    init {
        createInventorySlots(playerInv)
    }

    private fun createInventorySlots(pInventory: Inventory) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(pInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        for (k in 0..8) {
            this.addSlot(Slot(pInventory, k, 8 + k * 18, 142))
        }
    }
}