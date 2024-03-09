package com.github.zomb_676.cargo_hologram.ui.component

import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.ui.widget.CargoCheckBox
import com.github.zomb_676.cargo_hologram.ui.widget.CargoSlider
import com.github.zomb_676.cargo_hologram.util.ARGBColor
import com.github.zomb_676.cargo_hologram.util.BlurHandle
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics

object BlurConfigure {
    var blurRadius: Float = 20.0F
    var blurExpandY: Int = 10
    var blurBgAlpha: Int = 0x7f
    var blurOutline: Boolean = true

    fun render(guiGraphics: GuiGraphics, mainArea: AreaImmute) {
        BlurHandle.blur()
        val blurArea = mainArea.asBaseCursor().expandXMax().expandY(blurExpandY)
        BlurHandle.setBlurArea(blurArea)
        BlurHandle.setRadius(blurRadius)
        if (blurBgAlpha != 0) {
            guiGraphics.fill(
                blurArea.x1, blurArea.y1, blurArea.x2, blurArea.y2, ARGBColor.Vanilla.BLACK.alpha(
                    blurBgAlpha
                ).color
            )
        }
        if (blurOutline) {
            val boundaryColor = ARGBColor.Vanilla.BLACK.halfAlpha().color
            guiGraphics.hLine(blurArea.left, blurArea.right, blurArea.up, boundaryColor)
            guiGraphics.hLine(blurArea.left, blurArea.right, blurArea.down - 1, boundaryColor)
        }
    }

    fun radiusSlider() = CargoSlider.ofRange(0, 100, blurRadius.toInt())
        .withValueListener { r -> blurRadius = r.toFloat() }

    fun expandSlider(mainArea: AreaImmute) = CargoSlider.ofRange(0, mainArea.y1, blurExpandY)
        .withValueListener { expand -> blurExpandY = expand.toInt() }

    fun alphaSlider() = CargoSlider.ofRange(0, 0xff, blurBgAlpha)
        .withValueListener { alpha -> blurBgAlpha = alpha.toInt() }

    fun outlineCheckBox() = CargoCheckBox
        .ofExplicit(if (blurOutline) CargoCheckBox.State.CHECKED else CargoCheckBox.State.BANNED)
        .withListener { state -> blurOutline = state.value }

    fun onClose() {
        Config.Client.saveBlurConfigure()
    }

}