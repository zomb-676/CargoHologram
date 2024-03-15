package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.Config

interface CargoBlurScreen {
    enum class BlurType {
        DISABLE, MODERN_UI, SELF;
    }

    fun getBlurType(): BlurType = Config.Client.blurType
}