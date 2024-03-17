package com.github.zomb_676.cargo_hologram.ui

import net.minecraft.world.entity.player.StackedContents
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandlerModifiable
import net.minecraftforge.items.wrapper.RecipeWrapper

class WrappedCraftContainer(handle: IItemHandlerModifiable, private val width: Int, private val height: Int) :
    RecipeWrapper(handle), CraftingContainer {

    override fun fillStackedContents(pContents: StackedContents) {
        for (index in 0..8) {
            pContents.accountStack(this.getItem(index))
        }
    }

    override fun getWidth(): Int = width

    override fun getHeight(): Int = height

    override fun getItems(): List<ItemStack> = List(width * height, this::getItem)
}