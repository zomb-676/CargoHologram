package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.util.translate
import net.minecraft.network.chat.MutableComponent

object AllTranslates {
    private fun key(prefix: String, suffix: String): MutableComponent =
        "$prefix.${CargoHologram.MOD_ID}.$suffix".translate()

    private fun tab(name: String) = key("itemGroup", name)
    private fun uiTip(tip: String) = key("ui", tip)

    val MOD_TAB: MutableComponent = tab("items")

    val CONFIGURE_UI_TIP = uiTip("configure_ui_tip")
}