package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import com.github.zomb_676.cargo_hologram.trace.request.QueryRequirement
import com.github.zomb_676.cargo_hologram.trace.request.QuerySource
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class MonitorPanel : Item(Properties().stacksTo(1)) {
    companion object {
        var enable: ThreadLocal<Boolean> = ThreadLocal.withInitial { false }
            private set
    }

    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        enable.set(!enable.get())
        if (!pLevel.isClientSide) {
            pPlayer.sendSystemMessage("switch Monitor State -> ${if (enable.get()) "enable" else "disable"}".literal())
            if (enable.get()) {
                val source = QuerySource.ofPlayerCentered(
                    pPlayer as ServerPlayer,
                    2, QueryRequirement(true, crossDimension = true)
                )
                QueryCenter.appendSource(source)
            } else {
                QueryCenter.stopPlayer(pPlayer.uuid)
            }
        }
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand))
    }

}