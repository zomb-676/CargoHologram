package com.github.zomb_676.cargo_hologram.util

import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.mixin.PostChainAccessor
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.PostPass
import net.minecraft.client.renderer.RenderType
import net.minecraftforge.client.event.ScreenEvent
import java.util.*
import kotlin.math.max
import kotlin.math.min

object BlurHandle : BusSubscribe {
    private val BLUR_EFFECT_PATH = CargoHologram.rl("shaders/post/blur.json")
    private var underBlur = false
    private var radius : Float = 25f

    fun blur() {
        if (underBlur) return
        underBlur = true
        val mc = currentMinecraft()
        mc.level ?: return
        val gameRender = mc.gameRenderer
        gameRender.loadEffect(BLUR_EFFECT_PATH)
    }

    private fun forEachPass(pass: (pass: PostPass) -> Unit) {
        val effect = currentMinecraft().gameRenderer.currentEffect() ?: return
        val passes: List<PostPass> = (effect as PostChainAccessor).passes
        passes.forEach {
            pass(it)
        }
    }

    fun setRadius(radius: Float) {
        val toSetRadius = max(radius,1f)
        if (this.radius == toSetRadius) return
        this.radius = toSetRadius
        forEachPass { pass ->
            pass.effect.safeGetUniform("Radius").set(toSetRadius)
        }
    }

    fun setBlurArea(areaImmute: AreaImmute) {
        val window = currentMinecraft().window
        val left = areaImmute.x1.toFloat() / window.guiScaledWidth
        val right = areaImmute.x2.toFloat() / window.guiScaledWidth
        val top = 1.0f - (areaImmute.y1.toFloat() / window.guiScaledHeight)
        val down = 1.0f - (areaImmute.y2.toFloat() / window.guiScaledHeight)
        forEachPass { pass ->
            pass.effect.safeGetUniform("Area").set(left, right, top, down)
        }
    }

    fun drawScreenBackground(guiGraphics: GuiGraphics, blurArea: AreaImmute) {
        val buffer = guiGraphics.bufferSource().getBuffer(RenderType.gui())
        val pose = guiGraphics.pose().last().pose()
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<ScreenEvent.Init> { event ->

        }
        dispatcher<ScreenEvent.Closing> {
            val gameRenderer = currentMinecraft().gameRenderer
            if (Objects.equals(gameRenderer.currentEffect()?.name, BLUR_EFFECT_PATH.toString())) {
                gameRenderer.shutdownEffect()
            }
            underBlur = false
            radius = -1f
        }
    }
}
