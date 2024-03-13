package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.currentClientPlayer
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent
import net.minecraftforge.client.gui.overlay.ForgeGui
import net.minecraftforge.client.gui.overlay.IGuiOverlay

object CargoStorageHud : BusSubscribe, IGuiOverlay {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<RegisterGuiOverlaysEvent> { event ->
            event.registerAboveAll("cargo_storage", this)
        }
    }

    override fun render(gui: ForgeGui, guiGraphics: GuiGraphics, partialTick: Float, width: Int, height: Int) {
        if (gui.minecraft.screen != null) return
        val clientPlayer = currentClientPlayer()
        val res = clientPlayer.pick(5.0, partialTick, false)
        if (res.type != HitResult.Type.BLOCK) return
        val blockPos = (res as BlockHitResult).blockPos
        val storage = clientPlayer.level().getBlockEntity(blockPos)
                as CargoStorageBlockEntity? ?: return
        val displayItem = storage.displayItem

        val draw = AreaImmute.ofRelative(width / 2 + 5, height / 2 + 5, 100, 100)
            .asBaseCursor().forDraw(guiGraphics)
        if (!displayItem.isEmpty) draw.item(displayItem).nextLine()
        draw.string("Priority:${storage.priority}").nextLine()

        val traits = storage.traitList
        draw.string("mode:${traits.mode}").nextLine()
        if (traits.traits.isEmpty()) {
            draw.string("none trait set").nextLine()
        } else {
            for (trait in traits) {
                draw.string(trait.description).nextLine()
            }
        }

    }
}