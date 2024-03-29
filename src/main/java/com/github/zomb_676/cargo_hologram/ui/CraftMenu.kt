package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import com.github.zomb_676.cargo_hologram.trace.request.QueryRequirement
import com.github.zomb_676.cargo_hologram.trace.request.QuerySource
import com.github.zomb_676.cargo_hologram.util.OpenBy
import com.github.zomb_676.cargo_hologram.util.currentRegistryAccess
import com.github.zomb_676.cargo_hologram.util.currentServer
import com.github.zomb_676.cargo_hologram.util.retrive
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickAction
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.ItemStackHandler
import net.minecraftforge.items.SlotItemHandler
import org.apache.http.util.Asserts
import java.util.*

class CraftMenu(containerId: Int, val playerInv: Inventory, val openBy: OpenBy) :
    AbstractContainerMenu(AllRegisters.Menus.crafterManu.get(), containerId) {

    val materialHandle = ItemStackHandler(9)
    val resultHandle = ItemStackHandler(1)
    val materialSlots = Array(9) { index ->
        val x = index % 3
        val y = index / 3
        SlotItemHandler(materialHandle, index, 4 + x * 19 + 19 * 9 + 2, 136 + y * 19)
    }
    val resultSlot = SlotItemHandler(resultHandle, 0, 4 + 1 * 19 + 19 * 9 + 2, 136 + 3 * 19 + 2)
    private val craftContainer = WrappedCraftContainer(materialHandle, 3, 3)


    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack = ItemStack.EMPTY

    init {
        openBy.onMenuInit(playerInv)
        materialSlots.forEach(this::addSlot)
        this.addSlot(resultSlot)
        createInventorySlots(playerInv)

        if (!playerInv.player.level().isClientSide) {
            val source = QuerySource.ofPlayerCentered(
                playerInv.player as ServerPlayer,
                2, QueryRequirement(true, crossDimension = true)
            )
            QueryCenter.appendSource(source)
        }
    }

    private fun createInventorySlots(pInventory: Inventory) {
        for (i in 0..2) {
            for (j in 0..8) {
                this.addSlot(Slot(pInventory, j + i * 9 + 9, 4 + j * 19, 136 + i * 19))
            }
        }

        for (k in 0..8) {
            this.addSlot(Slot(pInventory, k, 4 + k * 19, 195))
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

    override fun tryItemClickBehaviourOverride(
        pPlayer: Player,
        pAction: ClickAction,
        pSlot: Slot,
        pClickedItem: ItemStack,
        pCarriedItem: ItemStack,
    ): Boolean {
        if (materialSlots.contains(pSlot)) {
            if (pCarriedItem.isEmpty) {
                setSlotItem(pSlot.index, ItemStack.EMPTY)
            } else {
                setSlotItem(pSlot.index, pCarriedItem.copyWithCount(1))
            }
            return true
        } else if (resultSlot == pSlot) {
            val result = resultSlot.item
            if (pCarriedItem.`is`(result.item)) {
                if (pCarriedItem.count + result.count > pCarriedItem.maxStackSize) return true
            }
            if (this.requestCraft()) {
                consumeEnergy(Config.Server.perCrftConsume * result.count / result.maxStackSize)
                if (pCarriedItem.isEmpty) {
                    carried = result.copy()
                } else if (pCarriedItem.`is`(result.item)) {
                    pCarriedItem.grow(result.count)
                } else {
                    pPlayer.addItem(result.copy())
                }
            }
            return true
        }
        return false
    }

    private fun requestCraft(): Boolean {
        val toSearch = mutableListOf<ItemStack>()
        materialSlots.forEach { m ->
            if (m.item.isEmpty) return@forEach
            val firstOrNull = toSearch.firstOrNull { it.`is`(m.item.item) }
            if (firstOrNull == null) {
                toSearch.add(m.item.copy())
            } else {
                firstOrNull.grow(m.item.count)
            }
        }
        val consumed = mutableListOf<ItemStack>()

        val player = playerInv.player
        val level = player.level()
        if (level.isClientSide) return false
        val querySource = QueryCenter.playerSources[player.uuid] ?: return false

        fun take(pos: BlockPos, slot: Int, item: ItemStack, count: Int): Boolean {
            var success = false
            level.getBlockEntity(pos)?.getCapability(ForgeCapabilities.ITEM_HANDLER)?.ifPresent { handle ->
                val stackInSlot = handle.getStackInSlot(slot)
                if (stackInSlot.`is`(item.item)) {
                    if (stackInSlot.count >= count) {
                        val extracted = handle.extractItem(slot, count, false)
                        consumed.add(extracted)
                        success = true
                    }
                }
            }
            return success
        }

        val success: Boolean = run {
            querySource.attached().forEach { (_, entry) ->
                val result = entry.result ?: return@forEach
                result.forEach { (pos, slots) ->
                    for ((slot, item) in slots) {
                        val index = toSearch.indexOfFirst { it.`is`(item.item) }
                        if (index != -1) {
                            val target = toSearch[index]
                            val success = if (item.count >= target.count) {
                                toSearch.remove(target)
                                take(pos, slot, item, target.count)
                            } else {
                                target.shrink(item.count)
                                take(pos, slot, item, item.count)
                            }
                            if (!success) {
                                consumed.forEach(player::addItem)
                                return@run false
                            }
                            if (toSearch.isEmpty()) {
                                return@run true
                            }
                        }
                    }
                }
            }
            true
        }
        if (!success || toSearch.isNotEmpty()) {
            consumed.forEach(player::addItem)
            val transCount = consumed.sumOf { it.count.toDouble() / it.maxStackSize }
            consumeEnergy((transCount * Config.Server.perItemTakeConsume).toInt())
        } else {
            val transCount = materialSlots.asSequence()
                .filter { it.hasItem() }
                .map { it.item }
                .sumOf { it.count.toDouble() / it.maxStackSize }
            consumeEnergy((transCount * Config.Server.perItemTakeConsume).toInt())
        }

        return success && toSearch.isEmpty()
    }

    override fun removed(pPlayer: Player) {
        super.removed(pPlayer)
        if (!pPlayer.level().isClientSide) {
            QueryCenter.stopPlayer(pPlayer.uuid)
        }
    }

    override fun clicked(pSlotId: Int, pButton: Int, pClickType: ClickType, pPlayer: Player) {
        if (pSlotId == playerInv.selected + 37 && pClickType != ClickType.THROW) return
        super.clicked(pSlotId, pButton, pClickType, pPlayer)
    }

    override fun stillValid(pPlayer: Player): Boolean {
        openBy.onItem {
            return holdAnd {
                getCapability(CapRegisters.CARGO_ENERGY_ITEM).retrive()?.remainPower() ?: false
            }
        }
        return false
    }

    private fun consumeEnergy(energy: Int) {
        openBy.onItem {
            holdItem().getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent { cap ->
                cap.current -= energy
            }
        }
    }

}