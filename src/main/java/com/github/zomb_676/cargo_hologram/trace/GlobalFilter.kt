package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.mixin.RandomizableContainerBlockEntityAccessor
import net.minecraft.world.level.block.entity.BlockEntity

object GlobalFilter {
    private fun isChestWithLoot(blockEntity: BlockEntity): Boolean {
        if (blockEntity is RandomizableContainerBlockEntityAccessor) {
            return blockEntity.lootTable != null
        }
        return true
    }

    fun filterBlockEntity(blockEntity: BlockEntity): Boolean {
        if (!Config.Server.allowLootChest) {
            if (isChestWithLoot(blockEntity)) {
                return false
            }
        }
        return true
    }
}