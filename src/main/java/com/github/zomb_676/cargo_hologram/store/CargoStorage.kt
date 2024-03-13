package com.github.zomb_676.cargo_hologram.store

import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.ui.CargoStorageMenu
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
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.network.NetworkHooks

class CargoStorage : Block(Properties.of()), EntityBlock {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): CargoStorageBlockEntity =
        CargoStorageBlockEntity(pPos, pState)

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
        NetworkHooks.openScreen(pPlayer as ServerPlayer, object : MenuProvider {
            override fun createMenu(
                pContainerId: Int,
                pPlayerInventory: Inventory,
                pPlayer: Player,
            ): AbstractContainerMenu =
                CargoStorageMenu(pContainerId, pPlayerInventory, pPos)

            override fun getDisplayName(): Component = "".literal()
        }, pPos)
        return InteractionResult.CONSUME
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRemove(
        pState: BlockState,
        pLevel: Level,
        pPos: BlockPos,
        pNewState: BlockState,
        pMovedByPiston: Boolean,
    ) {
        val blockEntity = pLevel.getBlockEntity(pPos) as CargoStorageBlockEntity? ?: return
        blockEntity.dropContent()
        pLevel.removeBlockEntity(pPos)
    }
}