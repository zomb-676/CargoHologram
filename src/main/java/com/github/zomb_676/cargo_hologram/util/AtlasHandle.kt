package com.github.zomb_676.cargo_hologram.util

import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation

object AtlasHandle {
    private var queryMap: Map<ResourceLocation, TextureAtlas> = mapOf()

    @JvmStatic
    fun updateQueryMap(byPath: Map<ResourceLocation, AbstractTexture>) {
        byPath.toMap().filterValues { it is TextureAtlas }
    }

    fun query(atlas: ResourceLocation) = queryMap[atlas]
}