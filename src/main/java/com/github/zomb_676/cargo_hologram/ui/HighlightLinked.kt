package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.layer
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.core.BlockPos
import net.minecraftforge.client.event.RenderLevelStageEvent

object HighlightLinked : BusSubscribe {
    var bind: BlockPos = BlockPos.ZERO
    var blocks: List<BlockPos> = listOf()
    val offset = 0.1

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<RenderLevelStageEvent> { event ->
            if (bind == BlockPos.ZERO || blocks.isEmpty()) return@dispatcher
            if (event.stage != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return@dispatcher

            val pose = event.poseStack
            val tesselator = Tesselator.getInstance()
            val builder = tesselator.builder
            val camPos = event.camera.position
            RenderSystem.setShader(GameRenderer::getPositionColorShader)
            RenderSystem.enableBlend()
            RenderSystem.disableDepthTest()
            builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR)

            pose.layer {
                pose.translate(-camPos.x, -camPos.y, -camPos.z)

                for (pos in blocks) {
                    val x = pos.x
                    val y = pos.y
                    val z = pos.z
                    LevelRenderer.addChainedFilledBoxVertices(
                        pose,
                        builder,
                        x + offset,
                        y + offset,
                        z + offset,
                        x + 1 - offset,
                        y + 1 - offset,
                        z + 1 - offset,
                        1.0f,
                        1.0f,
                        1.0f,
                        0.35f
                    )
                }
            }

            tesselator.end()
        }
    }

}