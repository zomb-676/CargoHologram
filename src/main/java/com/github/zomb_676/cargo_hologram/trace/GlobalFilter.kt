package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.mixin.RandomizableContainerBlockEntityAccessor
import com.github.zomb_676.cargo_hologram.selector.Selector
import com.github.zomb_676.cargo_hologram.util.ListMode
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import java.util.function.IntPredicate

object GlobalFilter {
    val ALWAYS_TRUE = IntPredicate { true }
    val ALWAYS_FALSE = IntPredicate { false }
    var allowLootChest = false
        private set
    private var testBlockEntity: (BlockEntityType<*>) -> Boolean = { true }
    private var testBlockEntityWithSlot: Map<BlockEntityType<*>, IntPredicate> = mutableMapOf()
    private var fallback: Boolean = true

    var globalListMode: ListMode = ListMode.IGNORE
        private set
    var globalSelectors: List<Selector> = emptyList()
        private set

    private fun isChestWithLoot(blockEntity: BlockEntity): Boolean =
        if (blockEntity is RandomizableContainerBlockEntityAccessor) {
            blockEntity.lootTable != null
        } else false

    fun filterBlockEntity(blockEntity: BlockEntity): Boolean {
        if (!allowLootChest) {
            if (isChestWithLoot(blockEntity)) {
                return false
            }
        }
        return testBlockEntity.invoke(blockEntity.type)
    }

    fun filterBlockEntitySlot(blockEntity: BlockEntity, slot: Int): Boolean {
        val predicate = testBlockEntityWithSlot[blockEntity.type] ?: return fallback
        return predicate.test(slot)
    }

    fun set(mode: ListMode, selectors: List<Selector>) {
        this.globalListMode = mode
        this.globalSelectors = selectors
        val specifiedBlockEntity = selectors.map(Selector::type)
        testBlockEntity = when (mode) {
            ListMode.BLACK_LIST_MODE -> { element -> !specifiedBlockEntity.contains(element) }
            ListMode.WHITE_LIST_MODE -> specifiedBlockEntity::contains
            ListMode.IGNORE -> { _ -> true }
        }
        testBlockEntityWithSlot = selectors.associate { selector ->
            selector.type to selector.asIntPredicate(mode)
        }
        fallback = mode.fallback
    }

    fun setAllowLootChest(allow: Boolean) {
        this.allowLootChest = allow
    }
}