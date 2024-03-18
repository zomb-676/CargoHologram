package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.CargoHologram

object UIConstant {
    const val ITEM_SIZE = 16
    const val ITEM_SIZE_WITH_PADDING = 18

    object Paths {
        private fun themeRL(path: String) =
            CargoHologram.rl(path)

        val cycleButton = themeRL("widget_rotate")

        val checkboxBanned     = themeRL("checkbox_banned")
        val checkboxChecked    = themeRL("checkbox_checked")
        val checkboxDefault    = themeRL("checkbox_default")

        val widgetNext         = themeRL("widget_next")
        val widgetRemove       = themeRL("widget_remove")
        val widgetSave         = themeRL("widget_save")

        val favouriteMark = themeRL("favourite_mark")
    }
}