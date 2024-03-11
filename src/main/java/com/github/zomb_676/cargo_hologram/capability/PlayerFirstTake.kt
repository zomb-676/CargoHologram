package com.github.zomb_676.cargo_hologram.capability

import net.minecraft.nbt.CompoundTag

class PlayerFirstTake {

    companion object {
        private const val TAG_KEY = "player_first_take"
    }

    var isPlayerFirstTake: Boolean = false

    fun copyFrom(source: PlayerFirstTake) {
        isPlayerFirstTake = source.isPlayerFirstTake
    }

    fun saveNBTData(tag: CompoundTag) {
        tag.putBoolean(TAG_KEY, isPlayerFirstTake)
    }

    fun loadNBTData(tag: CompoundTag) {
        isPlayerFirstTake = tag.getBoolean(TAG_KEY)
    }
}