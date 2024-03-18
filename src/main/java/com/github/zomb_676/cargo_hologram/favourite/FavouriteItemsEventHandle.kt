package com.github.zomb_676.cargo_hologram.favourite

import com.github.zomb_676.cargo_hologram.network.SetFavouritePack
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import com.mojang.blaze3d.systems.RenderSystem
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
        dispatcher<ScreenEvent.Render.Post> { event ->
            val screen = event.screen as? AbstractContainerScreen<*>? ?: return@dispatcher
            val menu = screen.menu
            val guiGraphics = event.guiGraphics
            val leftPos = screen.guiLeft
            val topPos = screen.guiTop
            menu.slots.forEach { slot ->
                val slotItem = slot.item
                if (slotItem.isEmpty) return@forEach
                if (!FavouriteItemUtils.isFavourite(slotItem)) return@forEach
                val x = slot.x + leftPos
                val y = slot.y + topPos
                guiGraphics.fillRelative(x, y, 16, 16, ARGBColor.Vanilla.BLUE)
            }
        }
    }

    private fun safeQueryMenuTypeIdentify(menu: AbstractContainerMenu): Any = try {
        menu.type
    } catch (e: UnsupportedOperationException) {
        menu::class.java.simpleName
    }

}