package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.blockEntity.CargoStorageBlockEntity
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
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult,
    ): InteractionResult {
        if (level.isClientSide) return InteractionResult.SUCCESS
        NetworkHooks.openScreen(player as ServerPlayer, object : MenuProvider {
            override fun createMenu(
                pContainerId: Int,
                pPlayerInventory: Inventory,
                pPlayer: Player,
            ): AbstractContainerMenu = CargoStorageMenu(pContainerId, pPlayerInventory, pos)

            override fun getDisplayName(): Component = "".literal()
        }, pos)
        return InteractionResult.CONSUME
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, byPiston: Boolean) {
        val blockEntity = level.getBlockEntity(pos) as CargoStorageBlockEntity? ?: return
        blockEntity.dropContent()
        level.removeBlockEntity(pos)
    }
}