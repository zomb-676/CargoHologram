package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.blockEntity.InserterBlockEntity
import com.github.zomb_676.cargo_hologram.ui.InserterMenu
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

class CargoInserter : Block(Properties.of().noOcclusion()), EntityBlock {
    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): InserterBlockEntity =
        InserterBlockEntity(pPos, pState)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun use(
        state: BlockState, level: Level, pos: BlockPos, player: Player,
        hand: InteractionHand, hit: BlockHitResult,
    ): InteractionResult {
        val item = player.getItemInHand(hand)
        if (item.`is`(AllRegisters.Items.linker.get())) {
            val entity = level.getBlockEntity(pos) as InserterBlockEntity? ?: return InteractionResult.PASS
            entity.setLinked(item)
            return InteractionResult.SUCCESS
        }

        if (level.isClientSide) return InteractionResult.SUCCESS
        NetworkHooks.openScreen(player as ServerPlayer, object : MenuProvider {
            override fun createMenu(
                pContainerId: Int,
                pPlayerInventory: Inventory,
                pPlayer: Player,
            ): AbstractContainerMenu =
                InserterMenu(pContainerId, pPlayerInventory, pos)

            override fun getDisplayName(): Component = "".literal()
        }, pos)
        return InteractionResult.CONSUME
    }
}