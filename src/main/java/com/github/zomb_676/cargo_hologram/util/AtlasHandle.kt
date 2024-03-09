package com.github.zomb_676.cargo_hologram.util

import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.resources.ResourceLocation

object AtlasHandle {
    private val queryMap: MutableMap<ResourceLocation, TextureAtlas> = mutableMapOf()

    @JvmStatic
    fun updateQueryMap(byPath: Map<ResourceLocation, AbstractTexture>) {
        queryMap.clear()
        for ((path, texture) in byPath) {
            if (texture is TextureAtlas) {
                queryMap[path] = texture
            }
        }
    }

    fun query(atlas: ResourceLocation) = queryMap[atlas]!!
}