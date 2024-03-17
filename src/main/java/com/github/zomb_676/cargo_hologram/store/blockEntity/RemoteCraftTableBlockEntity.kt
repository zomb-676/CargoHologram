package com.github.zomb_676.cargo_hologram.store.blockEntity

import com.github.zomb_676.cargo_hologram.AllRegisters.BlockEntities.remoteCraftTable
import com.github.zomb_676.cargo_hologram.Config
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState

class RemoteCraftTableBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(remoteCraftTable.get(), pos, state) {

    companion object {
        const val COMPOUND_MONITOR_DISTANCE = "monitor_distance"
    }

    var monitorDistance: Int = 2
        set(value) {
            field = value.coerceIn(-1, Config.Server.maxMonitorRadius)
        }

    override fun saveAdditional(pTag: CompoundTag) {
        super.saveAdditional(pTag)
        pTag.putInt(COMPOUND_MONITOR_DISTANCE, monitorDistance)
    }

    override fun load(pTag: CompoundTag) {
        super.load(pTag)
        monitorDistance = pTag.getInt(COMPOUND_MONITOR_DISTANCE)
    }


}