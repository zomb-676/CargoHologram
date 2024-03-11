package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.util.currentMinecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class UIConfigureItem(private val f: () -> Screen) : Item(Properties().stacksTo(1)) {
    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!pLevel.isClientSide) {
            val mc = currentMinecraft()
            mc.tell { mc.setScreen(f.invoke()) }
        }
        return super.use(pLevel, pPlayer, pUsedHand)
    }

    override fun isFoil(pStack: ItemStack): Boolean = true
}