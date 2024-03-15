package com.github.zomb_676.cargo_hologram

import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.resources.TextureAtlasHolder

class CargoHologramSpriteUploader(textureManager: TextureManager
) : TextureAtlasHolder(textureManager, ATLAS_LOCATION, ATLAS_INFO_LOCATION) {
    companion object {
        val ATLAS_LOCATION = CargoHologram.rl("textures/atlas/default_theme")
        private val ATLAS_INFO_LOCATION = CargoHologram.rl("gui")
    }
}