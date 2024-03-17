package com.github.zomb_676.cargo_hologram.favourite

import com.github.zomb_676.cargo_hologram.network.SetFavouritePack
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.FavouriteItemUtils
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent

object FavouriteItemsEventHandle : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<ScreenEvent.Init.Post> { event ->
//            attach auto move button
//            val button = Button.builder().build()
//            event.addListener(button)
        }
        dispatcher<ScreenEvent.MouseButtonPressed.Pre> { event ->
            if (!InteractHelper.ofButton(event.button).isLeft) return@dispatcher
            if (!Screen.hasControlDown()) return@dispatcher
            val screen = event.screen as? AbstractContainerScreen<*>? ?: return@dispatcher
            val slot = screen.slotUnderMouse ?: return@dispatcher
            if (!slot.hasItem()) return@dispatcher
            val slotItem = slot.item
            val current = FavouriteItemUtils.isFavourite(slotItem)
            SetFavouritePack(safeQueryMenuTypeIdentify(screen.menu), slot.index, slotItem, !current).sendToServer()
            event.isCanceled = true
        }
        dispatcher<ItemTooltipEvent> { event ->
            if (FavouriteItemUtils.isFavourite(event.itemStack)) {
                event.toolTip.add("favourite".literal())
            }
        }
    }

    private fun safeQueryMenuTypeIdentify(menu: AbstractContainerMenu): Any = try {
        menu.type
    } catch (e: UnsupportedOperationException) {
        menu::class.java.name
    }

}