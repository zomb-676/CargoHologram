package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.widget.CargoSlider
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.assign
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Inventory

class PreferConfigureScreen(menu: PreferConfigureMenu, inv: Inventory, component: Component) :
    AbstractContainerScreen<PreferConfigureMenu>(menu, inv, component), CargoBlurScreen {

    var mainArea = AreaImmute.ofFullScreen()
    lateinit var distanceSlider: CargoSlider

    override fun init() {
        mainArea = AreaImmute.ofFullScreen().asBaseCursor().percentX(0.5).percentY(0.6)
        distanceSlider = CargoSlider.ofRange(-1, Config.Server.maxMonitorRadius, 1)
        addRenderableWidget(distanceSlider)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(this, pGuiGraphics, mainArea)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.autoMove(false)
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(5)
        draw.assignUp(15).draw(pGuiGraphics) { draw ->
            draw.assignRight(120, distanceSlider::assign)
            draw.centeredString("monitor distance, 0 for current chunk")
        }

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun renderBg(pGuiGraphics: GuiGraphics, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {}

    override fun onClose() {
        super.onClose()
    }
}