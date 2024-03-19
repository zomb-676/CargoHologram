package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.blockEntity.RemoteCraftTableBlockEntity
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.ui.PreferConfigureMenu
import com.github.zomb_676.cargo_hologram.util.OpenBy
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkHooks

class RemoteCraftTable : Block(Properties.of()), EntityBlock {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pPlayer: Player,
        pHand: InteractionHand,
        pHit: BlockHitResult,
    ): InteractionResult {
        if (pLevel.isClientSide) return InteractionResult.SUCCESS
        val openBy = OpenBy.byBlock(pPos, pState, pLevel.dimension())
        NetworkHooks.openScreen(pPlayer as ServerPlayer, object : MenuProvider {
            override fun createMenu(
                pContainerId: Int,
                pPlayerInventory: Inventory,
                pPlayer: Player,
            ): AbstractContainerMenu = if (pPlayer.isShiftKeyDown) {
                PreferConfigureMenu(pContainerId, pPlayerInventory, openBy)
            } else {
                CraftMenu(pContainerId, pPlayerInventory, openBy)
            }

            override fun getDisplayName(): Component = "".literal()
        }, openBy::write)
        return InteractionResult.CONSUME
    }

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity =
        RemoteCraftTableBlockEntity(pPos, pState)
}