package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.ui.component.CargoSlider
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.assign
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen

class ConfigureScreen : Screen("Configure".literal()), CargoBlurScreen {

    var mainArea = AreaImmute.ofFullScreen()
    lateinit var blurSlider: CargoSlider
    lateinit var blurExpandSlider: CargoSlider
    lateinit var blurBgAlpha: CargoSlider

    override fun init() {
        mainArea = AreaImmute.ofFullScreen().asBaseCursor()
            .percentX(0.6).percentY(0.6)
        blurSlider = BlurConfigure.radiusSlider()
        blurExpandSlider = BlurConfigure.expandSlider(mainArea)
        blurBgAlpha = BlurConfigure.alphaSlider()
        addRenderableWidget(blurSlider)
        addRenderableWidget(blurExpandSlider)
        addRenderableWidget(blurBgAlpha)
    }

    @Suppress("NAME_SHADOWING")
    override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        BlurConfigure.render(pGuiGraphics, mainArea)
        val draw = mainArea.asBaseCursor().forDraw(pGuiGraphics)
        draw.autoMove(false)
        draw.outline(ARGBColor.Presets.WHITE)
        draw.inner(5)
        draw.assignUp(15).draw(pGuiGraphics) { draw ->
            draw.string("blur radius")
            draw.assignRight(120, blurSlider::assign)
        }
        draw.upDown(3)
        draw.assignUp(15).draw(pGuiGraphics) { draw ->
            draw.string("expand y")
            draw.assignRight(120, blurExpandSlider::assign)
        }
        draw.upDown(3)
        draw.assignUp(15).draw(pGuiGraphics) { draw ->
            draw.string("blur bg alpha")
            draw.assignRight(120, blurBgAlpha::assign)
        }
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick)
    }

    override fun onClose() {
        BlurConfigure.onClose()
        super.onClose()
    }
}