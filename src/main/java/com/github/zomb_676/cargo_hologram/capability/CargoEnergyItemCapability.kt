package com.github.zomb_676.cargo_hologram.capability

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.min

class CargoEnergyItemCapability(max: Int, current: Int = 0) : IEnergyStorage, INBTSerializable<CompoundTag> {
    constructor(tag: CompoundTag) : this(
        tag.getInt(COMPOUND_MAX_ENERGY_TAG_KE), tag.getInt(
            COMPOUND_CURRENT_ENERGY_TAK_KE
        )
    )

    companion object {
        const val COMPOUND_CURRENT_ENERGY_TAK_KE = "current"
        const val COMPOUND_MAX_ENERGY_TAG_KE = "max"
    }

    var max: Int = max
        private set
    var current: Int = current
        private set

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        val received = min(max - current, maxReceive)
        if (!simulate) current += received
        return received
    }

    override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int = 0
    override fun getEnergyStored(): Int = current
    override fun getMaxEnergyStored(): Int = max
    override fun canExtract(): Boolean = false
    override fun canReceive(): Boolean = current < max

    override fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt(COMPOUND_CURRENT_ENERGY_TAK_KE, current)
        tag.putInt(COMPOUND_MAX_ENERGY_TAG_KE, max)
        return tag
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        current = nbt.getInt(COMPOUND_CURRENT_ENERGY_TAK_KE)
        max = nbt.getInt(COMPOUND_MAX_ENERGY_TAG_KE)
    }

    fun energyRemainPercent() = current.toDouble() / max
}