package com.github.zomb_676.cargo_hologram.blockEntity

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.util.filter.ItemTrait
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.wrapper.CombinedInvWrapper

class CargoChargeTableBlockEntity(pos: BlockPos, state: BlockState) :
    BlockEntity(AllRegisters.BlockEntities.cargoChargeTable.get(), pos, state) {

    val chargeItemHandle = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean =
            stack.getCapability(CapRegisters.CARGO_ENERGY_ITEM).isPresent
    }

    val fuelItemHandle = object : ItemStackHandler(1) {
        override fun isItemValid(slot: Int, stack: ItemStack): Boolean = ItemTrait.FurnaceFuel.test(stack)
    }

    val combined = CombinedInvWrapper(chargeItemHandle, fuelItemHandle)

    override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> = when (cap) {
        ForgeCapabilities.ITEM_HANDLER -> LazyOptional.of { combined }
        ForgeCapabilities.ENERGY -> chargeItemHandle.getStackInSlot(0).getCapability(CapRegisters.CARGO_ENERGY_ITEM)
        else -> LazyOptional.empty()
    }.cast()
}