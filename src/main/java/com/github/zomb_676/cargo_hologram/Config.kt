package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.selector.Selector
import com.github.zomb_676.cargo_hologram.ui.CargoBlurScreen
import com.github.zomb_676.cargo_hologram.ui.component.BlurConfigure
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.config.ModConfigEvent

data object Config : BusSubscribe {

    data object Server : BusSubscribe {
        private val BUILDER = ForgeConfigSpec.Builder()

        private val ENABLE_DEBUG: ForgeConfigSpec.BooleanValue = BUILDER.define("enable_debug", false)

        private val MAXIMUM_MONITOR_RADIUS = BUILDER
            .comment("radius, when set 0, only the chunk where the player is can be monitored")
            .defineInRange("max_monitor_radius", 2, 0, 16)
        private val MAXIMUM_CRAFT_RADIUS = BUILDER
            .comment(
                "radius, when set 0, only the chunk where the player is can be monitored",
                "if greater than max_monitor_radius, will be clamped"
            )
            .defineInRange("max_craft_radius", 1, 0, 16)
        private val ALLOW_CROSS_DIMENSION_MONITOR = BUILDER
            .comment("player can monitor blocks in another dimension")
            .define("allow_cross_dimension_monitor", false)
        private val ALLOW_CROSS_DIMENSION_CRAFT = BUILDER
            .comment("player can monitor blocks in another dimension")
            .define("allow_cross_dimension_craft", false)
        private val ADMIN_EXEMPT = BUILDER
            .comment("op/admin will not be limited if set true")
            .define("admin_exempt", false)

        private val GLOBAL_LIST_MODE = BUILDER
            .comment(
                "only disable,black,white is valid",
                "when disabled, global_list is ignored"
            )
            .defineEnum("global_list_mode", ListMode.IGNORE)
        private val GLOBAL_LIST = BUILDER
            .comment(
                "each line is BlockEntityType's ResourceLocation,optional multi slot filter seperated by comma(,)",
                "for example minecraft:chest,1,2 means BlockEntityType with minecraft:chest and its slot index 1 and 2",
                "support interval selector, for example 1..4 means (1,4) ,2 and 3 is selected",
                "if you want end point is selected, use =",
                "for example, 1..=4 means 2, 3 and 4 is selected",
                "all slot index must greater or equal than 0(>=0)",
                "all space is ignored, note that ResourceLocation only allows small case(a_z) and under score(_)",
                "too many slot index filter for one BlockEntityType may cause candidate performance error",
                "1,2 count 2 and 1=..=3 count 1, use interval selector instead of successive number is better",
                "for all vanillas,under net.minecraft.world.level.block.entity.BlockEntityType, and minecraft namespace can be omitted",
            )
            .defineListAllowEmpty(
                "global_list",
                listOf("minecraft:chest", "jukebox")
            ) { str -> Selector.checkValid(str as String) }

        private val ALLOW_LOOT_CHEST = BUILDER
            .comment(
                "if blockEntity has Tag LootTable will not be scanned",
                "in code subclass of RandomizableContainerBlockEntity support this in vanilla"
            )
            .define("allow_loot_chest", false)

        private val SPEC: ForgeConfigSpec = BUILDER.build()

        override fun registerEvent(dispatcher: Dispatcher) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC)
            dispatcher<_>(::onLoad)
        }

        var enableDebug: Boolean = isOnDev()
            private set
        var maxMonitorRadius = 2
            private set
        var maxCraftRadius = 1
            private set
        var allowCrossDimMonitor: Boolean = false
            private set
        var allowCrossDimCraft: Boolean = false
            private set
        var adminExempt: Boolean = false
            private set
        var globalFilter: GlobalFilter = GlobalFilter(ListMode.IGNORE, listOf())
            private set
        var allowLootChest: Boolean = false

        private fun onLoad(@Suppress("UNUSED_PARAMETER") event: ModConfigEvent) {
            enableDebug = enableDebug or ENABLE_DEBUG.get()

            maxMonitorRadius = MAXIMUM_MONITOR_RADIUS.get()
            maxCraftRadius = MAXIMUM_CRAFT_RADIUS.get()
            if (maxCraftRadius > maxMonitorRadius) {
                maxCraftRadius = maxMonitorRadius
                MAXIMUM_CRAFT_RADIUS.set(maxCraftRadius)
            }

            allowCrossDimMonitor = ALLOW_CROSS_DIMENSION_MONITOR.get()
            allowCrossDimCraft = ALLOW_CROSS_DIMENSION_CRAFT.get()

            adminExempt = ADMIN_EXEMPT.get()

            val globalListMode = GLOBAL_LIST_MODE.get()
            val strings: List<String> = GLOBAL_LIST.get()
            val globalSelectors = strings.map(Selector::analyze)
            globalFilter = GlobalFilter(globalListMode, globalSelectors)

            allowLootChest = ALLOW_LOOT_CHEST.get()
        }
    }

    data object Client : BusSubscribe {
        private val BUILDER = ForgeConfigSpec.Builder()

        private val BLUR_TYPE = BUILDER
            .defineEnum("blur_type", CargoBlurScreen.BlurType.SELF)
        private val BLUR_RADIUS = BUILDER.push("blur_style")
            .defineInRange("blur_radius", 20.0, 0.1, 100.0)
        private val BLUR_EXPAND_Y = BUILDER
            .defineInRange("blur_expand_y", 10, 0, Int.MAX_VALUE)
        private val BLUR_BG_ALPHA = BUILDER
            .defineInRange("blur_bg_alpha", 0x7f, 0, 0xff)
        private val BLUR_OUTLINE = BUILDER
            .define("blur_outline", true)
        private val next = BUILDER.pop()

        private val SPEC: ForgeConfigSpec = BUILDER.build()

        override fun registerEvent(dispatcher: Dispatcher) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC)
            dispatcher<_>(::onLoad)
        }

        var blurType = CargoBlurScreen.BlurType.SELF
            private set

        private fun onLoad(@Suppress("UNUSED_PARAMETER") event: ModConfigEvent) {
            this.blurType = BLUR_TYPE.get()
            BlurConfigure.blurRadius = BLUR_RADIUS.get().toFloat()
            BlurConfigure.blurExpandY = BLUR_EXPAND_Y.get()
            BlurConfigure.blurBgAlpha = BLUR_BG_ALPHA.get()
            BlurConfigure.blurOutline = BLUR_OUTLINE.get()
        }

        fun saveBlurConfigure() {
            BLUR_RADIUS.set(BlurConfigure.blurRadius.toDouble())
            BLUR_EXPAND_Y.set(BlurConfigure.blurExpandY)
            BLUR_BG_ALPHA.set(BlurConfigure.blurBgAlpha)
            BLUR_OUTLINE.set(BlurConfigure.blurOutline)
        }
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        arrayOf(Server, Client).dispatch()
    }
}
