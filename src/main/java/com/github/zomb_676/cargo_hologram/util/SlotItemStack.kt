package com.github.zomb_676.cargo_hologram.util

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.item.ItemStack

data class SlotItemStack(val slot: Int, val itemStack: ItemStack) {
        fun encode(buffer: FriendlyByteBuf) {
            buffer.writeInt(slot)
            buffer.writeItemStack(itemStack, false)
        }

        companion object {
            fun decode(buffer: FriendlyByteBuf): SlotItemStack {
                val slot = buffer.readInt()
                val itemStack = buffer.readItem()
                return SlotItemStack(slot, itemStack)
            }
        }
    }