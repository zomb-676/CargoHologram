package com.github.zomb_676.cargo_hologram.util

import net.minecraft.world.item.ItemStack

object FavouriteItemUtils {
    private const val COMPOUND_FAVOURITE_KEY = "favourite"

    fun setFavourite(item: ItemStack, state: Boolean) {
        if (state) {
            item.orCreateTag.putBoolean(COMPOUND_FAVOURITE_KEY, true)
        } else {
            item.tag?.remove(COMPOUND_FAVOURITE_KEY)
        }
    }

    fun isFavourite(item: ItemStack): Boolean {
        val tag = item.tag ?: return false
        return tag.contains(COMPOUND_FAVOURITE_KEY) && tag.getBoolean(COMPOUND_FAVOURITE_KEY)
    }
}