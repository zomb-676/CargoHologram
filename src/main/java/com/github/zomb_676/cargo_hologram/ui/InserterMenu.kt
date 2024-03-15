package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.store.blockEntity.InserterBlockEntity
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler
import kotlin.math.tan

class InserterMenu(containerId: Int, val playerInv: Inventory, pos: BlockPos) :
    AbstractContainerMenu(AllRegisters.Menus.INSERTER_MENU.get(), containerId) {
    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    val tansHandle: ItemStackHandler = ItemStackHandler(36)
    val transSlots: Array<SlotItemHandler>
    val inserter: InserterBlockEntity

    init {
        createInventorySlots(playerInv)
        transSlots = Array(tansHandle.slots) { index ->
            val x = index % 9
            val y = index / 9
            SlotItemHandler(tansHandle, index, 4 + x * 19, y * 19 + 4)
        }
        transSlots.forEach(::addSlot)
        inserter = playerInv.player.level().getBlockEntity(pos) as? InserterBlockEntity? ?: throw RuntimeException()
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

    override fun stillValid(pPlayer: Player): Boolean = true

    fun transform() {
        val candidate = inserter.linked.map { (type, pos) ->
            inserter.level?.getBlockEntity(pos) as? CargoStorageBlockEntity?
        }.filterNotNull().sortedBy(CargoStorageBlockEntity::priority)
        this.transSlots.forEach { slot ->
            var transItem = slot.item
            if (transItem.isEmpty) return@forEach
            transItem = transItem.copy()
            val targetHandle = candidate.firstOrNull { it.traitList.test(slot.item) }?.handle ?: return@forEach
            for (index in 0..<targetHandle.slots) {
                transItem = targetHandle.insertItem(index, transItem, false)
                if (transItem.isEmpty) break
            }
            slot.set(transItem)
        }
    }

    override fun removed(pPlayer: Player) {
        super.removed(pPlayer)
        if (pPlayer is ServerPlayer) {
            transform()
            val give = pPlayer.isAlive && !pPlayer.hasDisconnected()
            this.transSlots.forEach { slot ->
                val slotItem = slot.item
                if (items.isEmpty()) return@forEach
                if (give) {
                    pPlayer.inventory.placeItemBackInInventory(slotItem)
                } else {
                    pPlayer.drop(slotItem, false)
                }
                slot.set(ItemStack.EMPTY)
            }
        }
    }
}