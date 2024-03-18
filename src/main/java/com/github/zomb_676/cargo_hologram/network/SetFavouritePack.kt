package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.util.FavouriteItemUtils
import com.github.zomb_676.cargo_hologram.util.location
import com.github.zomb_676.cargo_hologram.util.query
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
import net.minecraft.world.inventory.InventoryMenu

/**
 * @param menu menu like [CreativeModeInventoryScreen.ItemPickerMenu] doesn't have menu type, so
 * we should fall back to [String] by [Class.name] from [MenuType]
 */
class SetFavouritePack(val menu: Any, val slotIndex: Int, val item: ItemStack, val state: Boolean) :
    NetworkPack<SetFavouritePack> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): SetFavouritePack {
            val menu: Any = if (buffer.readBoolean()) {
                buffer.readResourceLocation().query(ForgeRegistries.MENU_TYPES)
            } else {
                buffer.readUtf()
            }
            val slot = buffer.readVarInt()
            val item = buffer.readItem()
            val state = buffer.readBoolean()
            return SetFavouritePack(menu, slot, item, state)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        when (menu) {
            is MenuType<*> -> {
                buffer.writeBoolean(true)
                buffer.writeResourceLocation(menu.location(ForgeRegistries.MENU_TYPES))
            }

            is String -> {
                buffer.writeBoolean(false)
                buffer.writeUtf(menu)
            }

            else -> throw RuntimeException()
        }
        buffer.writeVarInt(slotIndex)
        buffer.writeItemStack(item, false)
        buffer.writeBoolean(state)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            val container = context.sender?.containerMenu ?: return@enqueueWork
            if (!checkMenuType(container)) return@enqueueWork
            val slot = container.getSlot(slotIndex)
            val slotItem = slot.item
            if (ItemStack.isSameItem(slotItem, item)) {
                FavouriteItemUtils.setFavourite(slotItem, state)
            }
        }
    }

    private fun checkMenuType(another: AbstractContainerMenu): Boolean = when (menu) {
        is MenuType<*> -> menu == another.type
        "ItemPickerMenu" -> another is InventoryMenu
        is String -> menu == another::class.java.simpleName
        else -> throw RuntimeException()
    }

}