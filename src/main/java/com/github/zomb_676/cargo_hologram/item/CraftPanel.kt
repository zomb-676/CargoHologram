package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.network.NetworkHooks

class CraftPanel : Item(Properties().stacksTo(1)) {
    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!pLevel.isClientSide) {
            NetworkHooks.openScreen(pPlayer as ServerPlayer, object : MenuProvider {
                override fun createMenu(
                    pContainerId: Int,
                    pPlayerInventory: Inventory,
                    pPlayer: Player,
                ): AbstractContainerMenu = CraftMenu(pContainerId, pPlayerInventory)

                override fun getDisplayName(): Component = "".literal()
            })
        }
        return super.use(pLevel, pPlayer, pUsedHand)
    }
}