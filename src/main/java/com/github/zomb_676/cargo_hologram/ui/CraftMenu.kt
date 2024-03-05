package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.util.currentRegistryAccess
import com.github.zomb_676.cargo_hologram.util.currentServer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler
import org.apache.http.util.Asserts
import java.util.*

class CraftMenu(containerId: Int, val playerInv: Inventory) :
    AbstractContainerMenu(AllRegisters.CRAFTER_MANU.get(), containerId) {

    val materialHandle = ItemStackHandler(9)
    val resultHandle = ItemStackHandler(1)
    val craftSlots = Array(9) { index ->
        SlotItemHandler(materialHandle, index, index % 3, index / 3)
    }
    val resultSlot = SlotItemHandler(resultHandle, 0, 3, 1)
    private val craftContainer = WrappedCraftContainer(materialHandle, 3, 3)


    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun stillValid(pPlayer: Player): Boolean = true

    init {
        craftSlots.forEach(this::addSlot)
        this.addSlot(resultSlot)
        createInventorySlots(playerInv)
    }

    private fun createInventorySlots(pInventory: Inventory) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(pInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18))
            }
        }

        for (k in 0..8) {
            this.addSlot(Slot(pInventory, k, 8 + k * 18, 142))
        }
    }

    fun setRecipe(outputDisplay: List<Optional<ItemStack>>, inputDisplay: List<Optional<ItemStack>>) {
        Asserts.check(outputDisplay.size == 1, "craft output size must be 1")
        Asserts.check(inputDisplay.size == 9, "craft input size must be 9")
        inputDisplay.forEachIndexed { index, itemOptional ->
            itemOptional.ifPresentOrElse({ item -> materialHandle.setStackInSlot(index, item) }, {
                materialHandle.setStackInSlot(index, ItemStack.EMPTY)
            })
        }
        outputDisplay.first().ifPresentOrElse({ item -> resultHandle.setStackInSlot(0, item) }, {
            resultHandle.setStackInSlot(0, ItemStack.EMPTY)
        })
        sendAllDataToRemote()
    }

    fun setSlotItem(index: Int, itemStack: ItemStack) {
        materialHandle.setStackInSlot(index, itemStack)
        val result =
            currentServer().recipeManager.getRecipeFor(RecipeType.CRAFTING, craftContainer, playerInv.player.level())
        result.ifPresentOrElse({ r -> resultHandle.setStackInSlot(0, r.getResultItem(currentRegistryAccess())) },
            { resultHandle.setStackInSlot(0, ItemStack.EMPTY) })
        sendAllDataToRemote()
    }
}