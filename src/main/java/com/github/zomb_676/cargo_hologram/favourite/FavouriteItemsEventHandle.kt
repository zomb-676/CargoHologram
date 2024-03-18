package com.github.zomb_676.cargo_hologram.favourite

import com.github.zomb_676.cargo_hologram.CargoHologramSpriteUploader
import com.github.zomb_676.cargo_hologram.network.SetFavouritePack
import com.github.zomb_676.cargo_hologram.ui.UIConstant
import com.github.zomb_676.cargo_hologram.util.*
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.interact.InteractHelper
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
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
                guiGraphics.drawFavouriteIcon(x, y)
            }
            if (!menu.carried.isEmpty && FavouriteItemUtils.isFavourite(menu.carried)) {
                guiGraphics.drawFavouriteIcon(event.mouseX - 8, event.mouseY - 8)
            }
        }

        /**
         * @see net.minecraft.client.gui.Gui.renderHotbar for how to locate item location
         */
        dispatcher<RenderGuiOverlayEvent.Post> { event ->
            if (event.overlay != VanillaGuiOverlay.HOTBAR.type()) return@dispatcher
            val player = Minecraft.getInstance().player ?: return@dispatcher
            val guiGraphics = event.guiGraphics
            val items = player.inventory.items

            val halfScreenWidth = event.window.guiScaledWidth / 2
            val screenHeight = event.window.guiScaledHeight

            for (hotBarIndex in 0..<9) {
                val item = items[hotBarIndex]
                if (FavouriteItemUtils.isFavourite(item)) {
                    val pX = halfScreenWidth - 90 + hotBarIndex * 20 + 2
                    val pY = screenHeight - 16 - 3
                    guiGraphics.drawFavouriteIcon(pX, pY)
                }
            }
        }
    }

    private fun GuiGraphics.drawFavouriteIcon(x: Int, y: Int) {
        val sprite = AtlasHandle.query(CargoHologramSpriteUploader.ATLAS_LOCATION)
            .getSprite(UIConstant.Paths.favouriteMark)

        val minU = sprite.u0
        val maxU = sprite.u1
        val minV = sprite.v0
        val maxV = sprite.v1

        val pX1 = x.toFloat()
        val pX2 = (x + 8).toFloat()
        val pY1 = y.toFloat()
        val pY2 = (y + 8).toFloat()
        val z  = 300.0f

        RenderSystem.setShaderTexture(0, CargoHologramSpriteUploader.ATLAS_LOCATION)
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        val matrix =  this.pose().last().pose()
        val buffer = Tesselator.getInstance().builder
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX)
        buffer.vertex(matrix, pX1, pY1, z).uv(minU, minV).endVertex()
        buffer.vertex(matrix, pX1, pY2, z).uv(minU, maxV).endVertex()
        buffer.vertex(matrix, pX2, pY2, z).uv(maxU, maxV).endVertex()
        buffer.vertex(matrix, pX2, pY1, z).uv(maxU, minV).endVertex()
        BufferUploader.drawWithShader(buffer.end())
        RenderSystem.disableBlend()
    }

    private fun safeQueryMenuTypeIdentify(menu: AbstractContainerMenu): Any = try {
        menu.type
    } catch (e: UnsupportedOperationException) {
        menu::class.java.simpleName
    }

}