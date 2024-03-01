package com.github.zomb_676.cargo_hologram.ui.component

import com.github.zomb_676.cargo_hologram.util.asItemStack
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block

data class ItemComponent(val item: ItemStack, val decoration: Boolean = false) : TooltipComponent {
    //    constructor(block: BlockItem, decoration: Boolean) : this(block.asItemStack(), decoration)
    constructor(block: Block) : this(block.asItemStack())
    constructor(item: Item) : this(item.asItemStack())
}