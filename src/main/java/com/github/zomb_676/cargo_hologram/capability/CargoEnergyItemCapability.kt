package com.github.zomb_676.cargo_hologram.capability

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.energy.IEnergyStorage
import kotlin.math.min

class CargoEnergyItemCapability(max: Int, current: Int = 0) : IEnergyStorage, INBTSerializable<CompoundTag> {
    companion object {
        const val COMPOUND_CURRENT_ENERGY_TAK_KE = "current"
        const val COMPOUND_MAX_ENERGY_TAG_KE = "max"
        const val CHARGE_MAX_PER_TIME = 200

        fun of(tag: CompoundTag): CargoEnergyItemCapability {
            val capTag = tag.getCompound(CapRegisters.CARGO_ENERGY_ITEM_PATH.toString())
            val max = capTag.getInt(COMPOUND_MAX_ENERGY_TAG_KE)
            val current = capTag.getInt(COMPOUND_CURRENT_ENERGY_TAK_KE)
            return CargoEnergyItemCapability(max, current)
        }
    }

    var max: Int = max
        set(value) {
            field = 0.coerceAtLeast(value)
        }
    var current: Int = current
        set(value) {
            field = min(value, max)
        }

    override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
        val received = min(min(max - current, maxReceive), CHARGE_MAX_PER_TIME)
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
    fun remainPower(): Boolean = current > 0
}