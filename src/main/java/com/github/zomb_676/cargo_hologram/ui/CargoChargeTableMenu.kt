package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.blockEntity.CargoChargeTableBlockEntity
import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.util.OpenBy
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper
import kotlin.jvm.optionals.getOrNull

class CargoChargeTableMenu(containerId: Int, val playerInv: Inventory, val openBy: OpenBy) :
    AbstractContainerMenu(AllRegisters.Menus.cargoChargeTableMenu.get(), containerId) {

    val blockEntity: CargoChargeTableBlockEntity
    val chargeHandle: ItemStackHandler
    val chargeSlot: SlotItemHandler
    val fuelHandle: ItemStackHandler
    val fuelSlot: SlotItemHandler
    val curentDataSlot: DataSlot
    val maxDataSlot: DataSlot

    init {
        openBy.onMenuInit(playerInv)
        createInventorySlots(playerInv)
        blockEntity = openBy.expect<OpenBy.ByBlock>().expectBlockEntity()
        chargeHandle = blockEntity.chargeItemHandle
        chargeSlot = SlotItemHandler(chargeHandle, 0, 10, 10)
        addSlot(chargeSlot)
        fuelHandle = blockEntity.fuelItemHandle
        fuelSlot = SlotItemHandler(fuelHandle, 0, 10, 30)
        addSlot(fuelSlot)
        curentDataSlot = object : DataSlot() {
            override fun get(): Int = getChargeItemCapability()?.current ?: 0
            override fun set(pValue: Int) {
                val cap = getChargeItemCapability() ?: return
                cap.current = pValue
            }
        }
        addDataSlot(curentDataSlot)
        maxDataSlot = object : DataSlot() {
            override fun get(): Int = getChargeItemCapability()?.max ?: 0
            override fun set(pValue: Int) {
                val cap = getChargeItemCapability() ?: return
                cap.max = pValue
            }
        }
        addDataSlot(maxDataSlot)
    }

    private fun createInventorySlots(pInventory: Inventory) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(pInventory, j + i * 9 + 9, 4 + j * 19, 80 + i * 19 + 8))
            }
        }

        for (k in 0..8) {
            this.addSlot(Slot(pInventory, k, 4 + k * 19, 137 + 8 + 4))
        }
    }

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        val slotItem = slots[pIndex].item
        if (slotItem.isEmpty) return ItemStack.EMPTY
        if (pIndex in 0..<36) {
            slotItem.getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent { cap ->
                if (cap.current < cap.max) {
                    chargeSlot.set(slotItem)
                    slots[pIndex].set(ItemStack.EMPTY)
                }
            }
            return ItemStack.EMPTY
        } else {
            val transHandle = PlayerMainInvWrapper(pPlayer.inventory)
            var transItem = slotItem.copy()
            for (slotIndex in 0..<transHandle.slots) {
                transItem = transHandle.insertItem(slotIndex, transItem, false)
                if (transItem.isEmpty) break
            }
            this.slots[pIndex].set(transItem)
            return ItemStack.EMPTY
        }
    }

    override fun stillValid(pPlayer: Player): Boolean = openBy.expect<OpenBy.ByBlock>().distance(pPlayer)

    fun getChargeItemCapability() = chargeSlot.item.getCapability(CapRegisters.CARGO_ENERGY_ITEM).resolve().getOrNull()
}