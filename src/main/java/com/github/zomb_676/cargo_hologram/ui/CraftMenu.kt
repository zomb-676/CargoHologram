package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class CraftMenu(containerId: Int, playerInv: Inventory) :
    AbstractContainerMenu(AllRegisters.CRAFTER_MANU.get(), containerId) {

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(pPlayer: Player): Boolean = true

}