package com.github.zomb_676.cargo_hologram.util.filter

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

class SpecifiedItemTrait(val whiteMode: Boolean, val trait: ItemTrait) : Predicate<ItemStack> {
    operator fun component1() = whiteMode
    operator fun component2() = trait

    override fun test(itemStack: ItemStack): Boolean = trait.test(itemStack) == whiteMode

    val description = trait.description(whiteMode)

    companion object {
        private const val WHITE_MODE_TAG_KEY = "mode"

        fun readFromItem(itemStack: ItemStack): SpecifiedItemTrait {
            val trait = ItemTrait.readItemTrait(itemStack)
            val mode = ItemTrait.tag(itemStack).getBoolean(WHITE_MODE_TAG_KEY)
            return SpecifiedItemTrait(mode, trait)
        }

        fun readFromTag(tag: CompoundTag): SpecifiedItemTrait {
            val trait = ItemTrait.readItemTrait(tag)
            val mode = tag.getBoolean(WHITE_MODE_TAG_KEY)
            return SpecifiedItemTrait(mode, trait)
        }
    }

    fun writeToItem(itemStack: ItemStack) {
        trait.writeToItemNbt(itemStack)
        ItemTrait.tag(itemStack).putBoolean(WHITE_MODE_TAG_KEY, whiteMode)
    }

    fun writeToNbt(tag: CompoundTag) {
        trait.writeToNbt(tag)
        tag.putBoolean(WHITE_MODE_TAG_KEY, whiteMode)
    }
}