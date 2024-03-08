package com.github.zomb_676.cargo_hologram.mixin;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.lwjgl.opengl.GL45;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(PostChain.class)
public class PostChainMixin {
    @Shadow
    private float lastStamp;

    @Shadow
    private float time;

    @Shadow
    @Final
    private List<PostPass> passes;

    @Shadow
    @Final
    private String name;

    /**
     * @author zomb
     * @reason for test only
     */
    @Overwrite
    public void process(float pPartialTicks) {
        if (pPartialTicks < this.lastStamp) {
            this.time += 1.0F - this.lastStamp;
            this.time += pPartialTicks;
        } else {
            this.time += pPartialTicks - this.lastStamp;
        }

        for (this.lastStamp = pPartialTicks; this.time > 20.0F; this.time -= 20.0F) {
        }

        GL45.glPushDebugGroup(GL45.GL_DEBUG_SOURCE_APPLICATION, 0, "postChain:" + this.name);
        for (PostPass postpass : this.passes) {
            GL45.glPushDebugGroup(GL45.GL_DEBUG_SOURCE_APPLICATION, 0, "pass:" + postpass.getName());
            postpass.process(this.time / 20.0F);
            GL45.glPopDebugGroup();
        }
        GL45.glPopDebugGroup();

    }
}
