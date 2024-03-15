package com.github.zomb_676.cargo_hologram.util.filter

import com.github.zomb_676.cargo_hologram.mixin.DiggerItemAccessor
import com.github.zomb_676.cargo_hologram.util.literal
import com.github.zomb_676.cargo_hologram.util.location
import com.github.zomb_676.cargo_hologram.util.plus
import com.github.zomb_676.cargo_hologram.util.query
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.registries.ForgeRegistries
import java.util.*
import java.util.function.Predicate

sealed class ItemTrait : Predicate<ItemStack> {

    protected open fun rawDescription(pass: Boolean): String = throw NotImplementedError()
    open fun description(itemStack: ItemStack): Component = description(test(itemStack))
    open fun description(pass: Boolean): Component = rawDescription(pass).literal()
    open fun additionData(tag: CompoundTag) {}
    open fun shouldReplace(trait : ItemTrait) = this::class.java == trait::class.java
    fun writeToItem(itemStack: ItemStack) = writeToNbt(itemStack.orCreateTag)
    fun writeToNbt(tag: CompoundTag) {
        val innerTag = CompoundTag()
        innerTag.putString(COMPOUND_TYPE_KEY, this::class.java.simpleName)
        this.additionData(innerTag)
        tag.put(COMPOUND_NAME, innerTag)
    }

    fun writeToItemNbt(item: ItemStack) = writeToNbt(item.orCreateTag)

    override fun test(item: ItemStack): Boolean = TODO("Not yet implemented")

    data object Stackable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isStackable
        override fun rawDescription(pass: Boolean): String = if (pass) "stackable" else "un-stackable"
    }


    data object Placeable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.item is BlockItem
        override fun rawDescription(pass: Boolean): String = if (pass) "can be placed as a Block"
        else "can't be places as a Block"
    }

    data object Edible : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isEdible
        override fun rawDescription(pass: Boolean): String = if (pass) "is food" else "is not food"
    }

    data object FluidContainer : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent

        override fun rawDescription(pass: Boolean): String =
            if (pass) "can contain fluid" else "can't contain fluid"
    }

    data object Enchanted : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isEnchanted
        override fun rawDescription(pass: Boolean): String =
            if (pass) "enchanted" else "not enchanted"
    }

    data object MaxEnchanted : ItemTrait() {
        override fun test(item: ItemStack): Boolean =
            EnchantmentHelper.getEnchantments(item).asSequence().any { (enchant, level) -> level == enchant.maxLevel }

        override fun rawDescription(pass: Boolean): String =
            if (pass) "max enchanted" else "not max enchanted"
    }

    data object CustomName : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.hasCustomHoverName()

        override fun rawDescription(pass: Boolean): String =
            if (pass) "have custom name" else "not have custom name"
    }

    data object Damaged : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isDamaged
        override fun rawDescription(pass: Boolean): String =
            if (pass) "damaged" else "not damaged"
    }

    data object Damageable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isDamageableItem
        override fun rawDescription(pass: Boolean): String =
            if (pass) "damageable" else "un-damageable"
    }

    data object Equipable : ItemTrait() {
        override fun test(item: ItemStack): Boolean =
            LivingEntity.getEquipmentSlotForItem(item).type == EquipmentSlot.Type.ARMOR

        override fun rawDescription(pass: Boolean): String =
            if (pass) "equipment" else "not equipment"
    }

    data object FurnaceFuel : ItemTrait() {
        override fun test(item: ItemStack): Boolean = AbstractFurnaceBlockEntity.isFuel(item)

        override fun rawDescription(pass: Boolean): String =
            if (pass) "can be used as furnace fuel" else "can't be used as furnace fuel"
    }

    class ItemIdentity(val item : Item) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.`is`(this.item)
        override fun description(pass : Boolean): Component =
            if (pass) "is ".literal() + item.description else "is not ".literal() + item.description

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is ItemIdentity && trait.item == this.item

        override fun additionData(tag: CompoundTag) {
            tag.putString(COMPOUND_DATA_KEY, item.location(ForgeRegistries.ITEMS).toString())
        }

        companion object {
            fun fromItem(item: ItemStack): ItemIdentity = ItemIdentity(item.item)

            fun fromTag(tag : CompoundTag) : ItemIdentity {
                val string = tag.getString(COMPOUND_DATA_KEY)
                val item = ResourceLocation(string).query(ForgeRegistries.ITEMS)
                return ItemIdentity(item)
            }
        }
    }

    class ItemGroup(val tab: CreativeModeTab) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = tab.contains(item)
        override fun description(pass: Boolean): Component =
            if (pass) "under tab:".literal() + tab.displayName else "not under tab".literal() + tab.displayName

        override fun additionData(tag: CompoundTag) {
            tag.putString(COMPOUND_DATA_KEY, BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab)!!.toString())
        }

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is ItemGroup && trait.tab == this.tab

        companion object {
            fun fromItem(itemStack: ItemStack): List<ItemGroup> =
                CreativeModeTabs.tabs()
                    .asSequence()
                    .filterNot { it != CreativeModeTabs.searchTab() }
                    .filter { it.contains(itemStack) }
                    .map(::ItemGroup)
                    .toList()

            fun fromTag(tag: CompoundTag): ItemGroup {
                val string = tag.getString(COMPOUND_DATA_KEY)
                val tab = BuiltInRegistries.CREATIVE_MODE_TAB.get(ResourceLocation(string))!!
                return ItemGroup(tab)
            }
        }
    }

    class ItemTag(val itemTag: TagKey<Item>) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.`is`(itemTag)
        override fun rawDescription(pass: Boolean): String =
            if (pass) "have tag:${itemTag.location}" else "not have tag:${itemTag.location}"

        override fun additionData(tag: CompoundTag) =
            tag.putString(COMPOUND_DATA_KEY, itemTag.location.toString())

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is ItemTag && trait.itemTag.location == this.itemTag.location

        companion object {
            fun fromItem(itemStack: ItemStack): List<ItemTag> = itemStack.tags.map(::ItemTag).toList()
            fun fromTag(tag: CompoundTag): ItemTag {
                val str = tag.getString(COMPOUND_DATA_KEY)
                val tagKey = TagKey.create(Registries.ITEM, ResourceLocation(str))
                return ItemTag(tagKey)
            }
        }
    }

    class ModId(val modId: String) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = Objects.equals(item.item.getCreatorModId(item), modId)

        override fun rawDescription(pass: Boolean): String =
            if (pass) "added by mod:$modId" else "not added by mod:$modId"

        override fun additionData(tag: CompoundTag) =
            tag.putString(COMPOUND_DATA_KEY, modId)

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is ModId && trait.modId == this.modId

        companion object {
            fun fromItem(itemStack: ItemStack): ModId? {
                val id = itemStack.item.getCreatorModId(itemStack) ?: return null
                return ModId(id)
            }

            fun fromTag(tag: CompoundTag) =
                ModId(tag.getString(COMPOUND_DATA_KEY))
        }
    }

    class Color(val color: DyeColor) : ItemTrait() {
        override fun test(item: ItemStack): Boolean {
            if (Objects.equals(DyeColor.getColor(item), color)) return true
            val tag = item.tag ?: return false
//            when (val i = item.item) {
//                is FireworkRocketItem -> {
//                }
//            }
            return false
        }

        override fun rawDescription(pass: Boolean): String =
            if (pass) "dye color:${color.getName()}" else "not dye color:${color.getName()}"

        override fun additionData(tag: CompoundTag) =
            tag.putByte(COMPOUND_DATA_KEY, color.id.toByte())

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is Color && trait.color == this.color

        companion object {
            fun fromItem(itemStack: ItemStack): Color? {
                val dye = DyeColor.getColor(itemStack) ?: return null
                return Color(dye)
            }

            fun fromTag(tag: CompoundTag) =
                Color(DyeColor.byId(tag.getByte(COMPOUND_DATA_KEY).toInt()))
        }
    }

    class EquipSlot(val slot: EquipmentSlot) : ItemTrait() {
        override fun rawDescription(pass: Boolean): String =
            if (pass) "equip on ${slot.getName()}" else "not equip on ${slot.getName()}"

        override fun test(item: ItemStack): Boolean =
            LivingEntity.getEquipmentSlotForItem(item) == slot

        override fun additionData(tag: CompoundTag) =
            tag.putByte(COMPOUND_DATA_KEY, slot.ordinal.toByte())

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is EquipSlot && trait.slot == this.slot

        companion object {
            fun fromItem(itemStack: ItemStack) =
                EquipSlot(LivingEntity.getEquipmentSlotForItem(itemStack))

            fun fromTag(tag: CompoundTag) =
                EquipSlot(EquipmentSlot.entries[tag.getByte(COMPOUND_DATA_KEY).toInt()])
        }
    }

    class ToolType(val type: TagKey<Block>) : ItemTrait() {
        override fun rawDescription(pass: Boolean): String =
            if (pass) "tool type:${type.location}" else "not tool type:${type.location}"

        override fun test(item: ItemStack): Boolean {
            val actualItem = item.item
            return actualItem is DiggerItemAccessor && actualItem.blocks == type
        }

        override fun additionData(tag: CompoundTag) =
            tag.putString(COMPOUND_DATA_KEY, type.location.toString())

        override fun shouldReplace(trait: ItemTrait): Boolean =
            trait is ToolType && trait.type.location == this.type.location

        companion object {
            fun fromItem(item: ItemStack) =
                when (val i = item.item) {
                    is DiggerItemAccessor -> ToolType(i.blocks)
                    else -> null
                }

            fun fromTag(tag: CompoundTag): ToolType {
                val str = tag.getString(COMPOUND_DATA_KEY)
                val tagKey = TagKey.create(Registries.BLOCK, ResourceLocation(str))
                return ToolType(tagKey)
            }
        }
    }

    companion object {
        private const val COMPOUND_NAME = "item_trait"
        private const val COMPOUND_TYPE_KEY = "type"
        private const val COMPOUND_DATA_KEY = "data"

        private val dataTrait = listOf(
            Stackable, Placeable, Edible, FluidContainer, Enchanted, MaxEnchanted,
            CustomName, Damaged, Damageable, Equipable, FurnaceFuel
        )

        fun collect(item: ItemStack): List<ItemTrait> {
            val list = mutableListOf<ItemTrait>()
            fun append(traits: List<ItemTrait>) {
                traits.forEach { trait -> list += trait }
            }

            fun append(trait: ItemTrait?) {
                if (trait == null) return
                list += trait
            }
            append(dataTrait)
            append(ItemIdentity.fromItem(item))
            append(ToolType.fromItem(item))
            append(EquipSlot.fromItem(item))
            append(ItemGroup.fromItem(item))
            append(ItemTag.fromItem(item))
            append(ModId.fromItem(item))
            append(Color.fromItem(item))
            return list
        }

        fun readItemTrait(tag: CompoundTag): ItemTrait {
            val tag = tag.getCompound(COMPOUND_NAME)
            return when (val name = tag.getString(COMPOUND_TYPE_KEY)) {
                "Stackable" -> Stackable
                "Placeable" -> Placeable
                "Edible" -> Edible
                "FluidContainer" -> FluidContainer
                "Enchanted" -> Enchanted
                "MaxEnchanted" -> MaxEnchanted
                "CustomName" -> CustomName
                "Damaged" -> Damaged
                "Damageable" -> Damageable
                "Equipable" -> Equipable
                "FurnaceFuel" -> FurnaceFuel
                "ItemIdentity" -> ItemIdentity.fromTag(tag)
                "ItemGroup" -> ItemGroup.fromTag(tag)
                "ItemTag" -> ItemTag.fromTag(tag)
                "ModId" -> ModId.fromTag(tag)
                "Color" -> Color.fromTag(tag)
                "EquipSlot" -> EquipSlot.fromTag(tag)
                "ToolType" -> ToolType.fromTag(tag)
                else -> throw RuntimeException("unknown tag type:$name")
            }
        }

        fun readItemTrait(item: ItemStack) = readItemTrait(item.tag!!)

        fun tag(itemStack: ItemStack) = tag(itemStack.tag!!)
        fun tag(tag: CompoundTag) = tag.getCompound(COMPOUND_NAME)
    }
}