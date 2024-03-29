package com.github.zomb_676.cargo_hologram.util.filter

import com.github.zomb_676.cargo_hologram.util.filter.TraitList.TraitMode.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.util.INBTSerializable
import java.util.function.Predicate

class TraitList(traits: List<SpecifiedItemTrait>, mode: TraitMode) : INBTSerializable<CompoundTag>,
    Iterable<SpecifiedItemTrait>, Predicate<ItemStack> {

    constructor() : this(listOf(), PASS_ANY)

    enum class TraitMode {
        PASS_ANY,
        PASS_ALL,
        PASS_NONE,
    }

    companion object {
        private const val TRAIT_LIST_TAG_NAME = "trait_list"
        private const val TRAIT_LIST_CONTENT_TAG_NAME = "trait_list_content"
        private const val TRAIT_LIST_MODE_NAME = "trait_mode"

        fun contains(itemStack: ItemStack) =
            itemStack.tag?.contains(TRAIT_LIST_TAG_NAME, Tag.TAG_COMPOUND.toInt()) ?: false

        fun removeTag(filterItem: ItemStack) {
            val tag = filterItem.tag ?: return
            tag.remove(TRAIT_LIST_TAG_NAME)
        }
    }

    val traits: MutableList<SpecifiedItemTrait> = traits.toMutableList()
    var mode: TraitMode = PASS_ANY

    override fun serializeNBT(): CompoundTag {
        val tag = CompoundTag()
        val listTag = ListTag()
        for (trait in traits) {
            val traitTag = CompoundTag()
            trait.writeToNbt(traitTag)
            listTag.add(traitTag)
        }
        tag.put(TRAIT_LIST_CONTENT_TAG_NAME, listTag)
        tag.putInt(TRAIT_LIST_MODE_NAME, mode.ordinal)
        return CompoundTag().apply { put(TRAIT_LIST_TAG_NAME, tag) }
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        traits.clear()
        val compound = nbt.getCompound(TRAIT_LIST_TAG_NAME)
        val tagList = compound.getList(TRAIT_LIST_CONTENT_TAG_NAME, Tag.TAG_COMPOUND.toInt())
        tagList.forEach { tag ->
            traits.add(SpecifiedItemTrait.readFromTag(tag as CompoundTag))
        }
        mode = TraitMode.entries[compound.getInt(TRAIT_LIST_MODE_NAME)]
    }

    fun writeToItem(filterItem: ItemStack) {
        val tag = filterItem.orCreateTag
        tag.put(TRAIT_LIST_TAG_NAME, serializeNBT().getCompound(TRAIT_LIST_TAG_NAME))
    }

    fun readFromItem(filterItem: ItemStack) {
        if (TraitList.contains(filterItem)) {
            deserializeNBT(filterItem.tag!!)
        }
    }

    fun appendTrait(trait: SpecifiedItemTrait) {
        val index = traits.indexOfFirst { it.trait.shouldReplace(trait.trait) }
        if (index == -1) {
            traits.add(trait)
        } else {
            traits[index] = trait
        }
    }

    override fun iterator(): Iterator<SpecifiedItemTrait> = traits.iterator()

    fun traits(): List<SpecifiedItemTrait> = traits

    override fun test(itemStack: ItemStack): Boolean {
        if (traits.isEmpty()) return true
        return when (mode) {
            PASS_ANY -> traits.any { it.test(itemStack) }
            PASS_ALL -> traits.all { it.test(itemStack) }
            PASS_NONE -> traits.none { it.test(itemStack) }
        }
    }

    fun calcuateSize(): Int {
        if (traits.isEmpty()) return 36
        val baseSize = 9.0
        val modeFactor = if (traits.size == 1) 1.1 else when (mode) {
            PASS_ANY -> 1.5
            PASS_ALL -> 1.8
            PASS_NONE -> 1.2
        }

        val traitFactor = TraitSizeCalculate.traitFactor(this)

        val result = baseSize * modeFactor * traitFactor

        return result.toInt().coerceAtMost(360)
    }
}