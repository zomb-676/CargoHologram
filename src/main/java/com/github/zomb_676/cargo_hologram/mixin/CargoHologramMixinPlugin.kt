package com.github.zomb_676.cargo_hologram.mixin

import net.minecraftforge.fml.loading.FMLLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo

class CargoHologramMixinPlugin : IMixinConfigPlugin {
    override fun onLoad(mixinPackage: String) {}

    override fun getRefMapperConfig(): String? = null

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean = true

    override fun acceptTargets(myTargets: MutableSet<String>, otherTargets: Set<String>) {
//        if (FMLLoader.getLoadingModList().getModFileById("modernui") != null) {
//            myTargets += "com.github.zomb_676.cargo_hologram.mixin.compact.ModernUIBlurHandleMixin"
//        }
    }

    override fun getMixins(): MutableList<String>? = null

    override fun preApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo,
    ) {
    }

    override fun postApply(
        targetClassName: String,
        targetClass: ClassNode,
        mixinClassName: String,
        mixinInfo: IMixinInfo,
    ) {
    }
}