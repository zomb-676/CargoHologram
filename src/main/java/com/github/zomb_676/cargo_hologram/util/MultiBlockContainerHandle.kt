package com.github.zomb_676.cargo_hologram.util

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet
import net.minecraft.core.Direction
import net.minecraft.world.level.block.ChestBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.state.properties.ChestType
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.IItemHandler

object MultiBlockContainerHandle {

    private fun <T : BlockEntity> T.neighbour(direction: Direction): T {
        val relative = this.blockPos.relative(direction)
        val relativeBlockEntity = this.level!!.getBlockEntity(relative)!!
        return relativeBlockEntity as T
    }

    private inline fun BlockEntity.queryItemHandler(crossinline handle: (IItemHandler) -> Unit) {
        this.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent {
            handle(it)
        }
    }

    fun handle(alreadySearched: IntAVLTreeSet, itemHandle: IItemHandler, identityHash: Int, blockEntity: BlockEntity) {
        if (blockEntity is ChestBlockEntity) {
            val blockState = blockEntity.blockState
            val property = blockState.getValue(ChestBlock.TYPE)
            if (property != ChestType.SINGLE) {
                val direction = ChestBlock.getConnectedDirection(blockState)
                val neighbour = blockEntity.neighbour(direction)
                neighbour.queryItemHandler { handle ->
                    alreadySearched.add(handle.hashCode())
                }
            }
            alreadySearched.add(itemHandle.hashCode())
        } else {
            alreadySearched.add(identityHash)
        }
    }
}