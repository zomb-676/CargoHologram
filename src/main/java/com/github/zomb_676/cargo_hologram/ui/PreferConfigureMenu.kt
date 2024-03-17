package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.store.blockEntity.RemoteCraftTableBlockEntity
import com.github.zomb_676.cargo_hologram.util.OpenBy
import com.github.zomb_676.cargo_hologram.util.throwOnDev
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class PreferConfigureMenu(containerId: Int, val playerInv: Inventory, val openBy: OpenBy) :
    AbstractContainerMenu(AllRegisters.Menus.PREFER_CONFIGURE_MENU.get(), containerId) {

    companion object {

    }

    init {
        openBy.onMenuInit(playerInv)
        openBy.onBlock {
            expect<RemoteCraftTableBlockEntity>()
        }
    }

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack = ItemStack.EMPTY
    override fun stillValid(pPlayer: Player): Boolean =
        openBy.expect<OpenBy.ByBlock>().distance(pPlayer)
}