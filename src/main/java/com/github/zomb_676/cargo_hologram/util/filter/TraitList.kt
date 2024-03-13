package com.github.zomb_676.cargo_hologram.util.filter

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.util.INBTSerializable

class TraitList(traits: List<SpecifiedItemTrait>, mode: TraitMode) : INBTSerializable<CompoundTag>,
    Iterable<SpecifiedItemTrait> {

    constructor() : this(listOf(), TraitMode.PASS_ANY)

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
    var mode: TraitMode = TraitMode.PASS_ANY
        private set

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
}