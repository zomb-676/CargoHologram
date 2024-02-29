package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.ui.MonitorScreen
import com.github.zomb_676.cargo_hologram.util.open
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class CargoMonitor : Item(Properties().stacksTo(1)) {
    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (pLevel.isClientSide) {
            MonitorScreen().open()
        }
        return super.use(pLevel, pPlayer, pUsedHand)
    }
}