package com.github.zomb_676.cargo_hologram.util.filter

import com.github.zomb_676.cargo_hologram.mixin.DiggerItemAccessor
import com.github.zomb_676.cargo_hologram.util.literal
import com.github.zomb_676.cargo_hologram.util.plus
import net.minecraft.network.chat.Component
import net.minecraft.tags.TagKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.*
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity
import net.minecraftforge.common.capabilities.ForgeCapabilities
import java.util.*
import java.util.function.Predicate

sealed class ItemTrait : Predicate<ItemStack> {


    open fun rawDescription(itemStack: ItemStack): String = TODO()
    open fun description(itemStack: ItemStack): Component = rawDescription(itemStack).literal()

    override fun test(item: ItemStack): Boolean = TODO("Not yet implemented")

    data object Stackable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isStackable
        override fun rawDescription(itemStack: ItemStack): String = if (test(itemStack)) "stackable" else "un-stackable"
    }

    data object Placeable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.item is BlockItem
        override fun rawDescription(itemStack: ItemStack): String = if (test(itemStack)) "can be placed as a Block"
        else "can't be places as a Block"
    }

    data object Edible : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isEdible
        override fun rawDescription(itemStack: ItemStack): String = if (test(itemStack)) "is food" else "is not food"
    }

    data object FluidContainer : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "can contain fluid" else "can't contain fluid"
    }

    data object Enchanted : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isEnchanted
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "enchanted" else "not enchanted"
    }

    data object MaxEnchanted : ItemTrait() {
        override fun test(item: ItemStack): Boolean =
            EnchantmentHelper.getEnchantments(item).asSequence().any { (enchant, level) -> level == enchant.maxLevel }

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "max enchanted" else "not max enchanted"
    }

    data object CustomName : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.hasCustomHoverName()

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "have custom name" else "not have custom name"
    }

    data object Damaged : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isDamaged
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "damaged" else "not damaged"
    }

    data object Damageable : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.isDamageableItem
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "damageable" else "un-damageable"
    }

    data object Equipable : ItemTrait() {
        override fun test(item: ItemStack): Boolean =
            LivingEntity.getEquipmentSlotForItem(item).type == EquipmentSlot.Type.ARMOR

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "equipment" else "not equipment"
    }

    data object FurnaceFuel : ItemTrait() {
        override fun test(item: ItemStack): Boolean = AbstractFurnaceBlockEntity.isFuel(item)

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "can be used as furnace fuel" else "can't be used as furnace fuel"
    }

    class ItemGroup(val tab: CreativeModeTab) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = tab.contains(item)
        override fun description(itemStack: ItemStack): Component =
            if (test(itemStack)) "under tab:".literal() + tab.displayName else "not under tab".literal() + tab.displayName


        companion object {
            fun fromItem(itemStack: ItemStack): List<ItemGroup> =
                CreativeModeTabs.tabs().asSequence().filter { it.contains(itemStack) }.map(::ItemGroup).toList()
        }
    }

    class ItemTag(val tag: TagKey<Item>) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = item.`is`(tag)
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "have tag:${tag.location}" else "not have tag:${tag.location}"

        companion object {
            fun fromItem(itemStack: ItemStack): List<ItemTag> = itemStack.tags.map(::ItemTag).toList()
        }
    }

    class ModId(val modId: String) : ItemTrait() {
        override fun test(item: ItemStack): Boolean = Objects.equals(item.item.getCreatorModId(item), modId)

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "added by mod:$modId" else "not added by mod:$modId"

        companion object {
            fun fromItem(itemStack: ItemStack): ModId? {
                val id = itemStack.item.getCreatorModId(itemStack) ?: return null
                return ModId(id)
            }
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

        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "dye color:${color.getName()}" else "not dye color:${color.getName()}"

        companion object {
            fun fromItem(itemStack: ItemStack): Color? {
                val dye = DyeColor.getColor(itemStack) ?: return null
                return Color(dye)
            }
        }
    }

    class EquipSlot(val slot: EquipmentSlot) : ItemTrait() {
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "equip on ${slot.getName()}" else "not equip on ${slot.getName()}"

        override fun test(item: ItemStack): Boolean =
            LivingEntity.getEquipmentSlotForItem(item) == slot

        companion object {
            fun fromItem(itemStack: ItemStack) =
                EquipSlot(LivingEntity.getEquipmentSlotForItem(itemStack))
        }
    }

    class ToolType(val type: TagKey<Block>) : ItemTrait() {
        override fun rawDescription(itemStack: ItemStack): String =
            if (test(itemStack)) "tool type:${type.location}" else "not tool type:${type.location}"

        override fun test(item: ItemStack): Boolean {
            val actualItem = item.item
            return actualItem is DiggerItemAccessor && actualItem.blocks == type
        }

        companion object {
            fun fromItem(item: ItemStack) =
                when (val i = item.item) {
                    is DiggerItemAccessor -> ToolType(i.blocks)
                    else -> null
                }
        }
    }

    companion object {
        private val dataTrait = listOf(
            Stackable, Placeable, Edible, FluidContainer, Enchanted, MaxEnchanted,
            CustomName, Damaged, Damageable, Equipable, FurnaceFuel
        )

        fun collect(item: ItemStack): MutableMap<ItemTrait, Component> {
            val list = mutableMapOf<ItemTrait, Component>()
            fun append(traits: List<ItemTrait>) {
                traits.forEach { trait -> list[trait] = trait.description(item) }
            }

            fun append(trait: ItemTrait?) {
                if (trait == null) return
                list[trait] = trait.description(item)
            }
            append(dataTrait)
            append(ToolType.fromItem(item))
            append(EquipSlot.fromItem(item))
            append(ItemGroup.fromItem(item))
            append(ItemTag.fromItem(item))
            append(ModId.fromItem(item))
            append(Color.fromItem(item))
            return list
        }

    }
}