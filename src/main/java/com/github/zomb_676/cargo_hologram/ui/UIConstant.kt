package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.CargoHologram

object UIConstant {
    const val ITEM_SIZE = 16
    const val ITEM_SIZE_WITH_PADDING = 18

    @Suppress("unused")
    object Paths {
        private fun themeRL(path: String) =
            CargoHologram.rl(path)

        val background         = themeRL("background")

        val buttonLongDefault  = themeRL("button_long_default")
        val buttonLongHover    = themeRL("button_long_hover")
        val buttonLongPressed  = themeRL("button_long_pressed")

        val buttonSmallDefault = themeRL("button_small_default")
        val buttonSmallHover   = themeRL("button_small_hover")
        val buttonSmallPressed = themeRL("button_small_pressed")

        val checkboxBanned     = themeRL("checkbox_banned")
        val checkboxChecked    = themeRL("checkbox_checked")
        val checkboxDefault    = themeRL("checkbox_default")

        val deco1              = themeRL("deco_1")
        val deco2              = themeRL("deco_2")
        val deco3              = themeRL("deco_3")
        val deco4              = themeRL("deco_4")

        val editorLabelDefault = themeRL("editor_label_default")
        val editorLabelHover   = themeRL("editor_label_hover")
        val editorLayerBox     = themeRL("editor_layer_box")

        val fluidTank          = themeRL("fluid_tank")

        val icon               = themeRL("icon")
        val iconFocus          = themeRL("icon_focus")
        val iconMissing        = themeRL("icon_missing")
        val iconStyleSelect    = themeRL("icon_style_select")

        val inventory          = themeRL("inventory")

        val itemSlot           = themeRL("item_slot")
        val itemSlotLarge      = themeRL("item_slot_large")

        val layerDown          = themeRL("layer_down")
        val layerInfo          = themeRL("layer_info")
        val layerLockOff       = themeRL("layer_lock_off")
        val layerLockOn        = themeRL("layer_lock_on")
        val layerUp            = themeRL("layer_up")
        val layerVisOff        = themeRL("layer_vis_off")
        val layerVisOn         = themeRL("layer_vis_on")

        val menu3D             = themeRL("menu_3d")
        val menuButton         = themeRL("menu_button")
        val menuCheckbox       = themeRL("menu_checkbox")
        val menuContainer      = themeRL("menu_container")
        val menuDecoration     = themeRL("menu_decoration")
        val menuEnergy         = themeRL("menu_energy")
        val menuHolder         = themeRL("menu_holder")
        val menuInput          = themeRL("menu_input")
        val menuMeter          = themeRL("menu_meter")
        val menuMisc           = themeRL("menu_misc")
        val menuProcess        = themeRL("menu_process")
        val menuSlide          = themeRL("menu_slide")
        val menuSlot           = themeRL("menu_slot")
        val menuTank           = themeRL("menu_tank")
        val menuText           = themeRL("menu_text")

        val meterStyle1        = themeRL("meter_style_1")
        val meterStyle1Fill    = themeRL("meter_style_1_fill")

        val meterStyle2        = themeRL("meter_style_2")
        val meterStyle2Fill    = themeRL("meter_style_2_fill")

        val meterStyle3        = themeRL("meter_style_3")
        val meterStyle3Fill    = themeRL("meter_style_3_fill")

        val playerInventory    = themeRL("player_inventory")
        val previewBar3D       = themeRL("preview_bar_3d")
        val sliderButton       = themeRL("slider_button")
        val sliderRail         = themeRL("slider_rail")
        val textBar            = themeRL("text_bar")
        val textBarFocus       = themeRL("text_bar_focus")

        val widgetAdd          = themeRL("widget_add")
        val widgetAutoscale    = themeRL("widget_autoscale")
        val widgetDiscard      = themeRL("widget_discard")
        val widgetExport       = themeRL("widget_export")
        val widgetExportFocus  = themeRL("widget_export_focus")
        val widgetImport       = themeRL("widget_import")
        val widgetLast         = themeRL("widget_last")
        val widgetNext         = themeRL("widget_next")
        val widgetRemove       = themeRL("widget_remove")
        val widgetRotate       = themeRL("widget_rotate")
        val widgetSave         = themeRL("widget_save")
        val widgetWarning      = themeRL("widget_warning")
    }
}