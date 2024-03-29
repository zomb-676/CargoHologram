package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.selector.Selector
import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
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

        private val MAXIMUM_MONITOR_RADIUS =
            BUILDER.comment("radius, when set 0, only the chunk where the player is can be monitored")
                .defineInRange("max_monitor_radius", 2, 0, 16)
        private val MAXIMUM_CRAFT_RADIUS = BUILDER.comment(
            "radius, when set 0, only the chunk where the player is can be monitored",
            "if greater than max_monitor_radius, will be clamped"
        ).defineInRange("max_craft_radius", 1, 0, 16)
        private val ALLOW_CROSS_DIMENSION_MONITOR = BUILDER.comment("player can monitor blocks in another dimension")
            .define("allow_cross_dimension_monitor", false)
        private val ALLOW_CROSS_DIMENSION_CRAFT = BUILDER.comment("player can monitor blocks in another dimension")
            .define("allow_cross_dimension_craft", false)
        private val ADMIN_EXEMPT =
            BUILDER.comment("op/admin will not be limited if set true").define("admin_exempt", false)

        private val GLOBAL_LIST_MODE = BUILDER.comment(
            "only disable,black,white is valid", "when disabled, global_list is ignored"
        ).defineEnum("global_list_mode", ListMode.IGNORE)
        private val GLOBAL_LIST = BUILDER.comment(
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
        ).defineListAllowEmpty(
            "global_list", listOf("minecraft:chest", "jukebox")
        ) { str -> Selector.checkValid(str as String) }

        private val ALLOW_LOOT_CHEST = BUILDER.comment(
            "if blockEntity has Tag LootTable will not be scanned",
            "in code subclass of RandomizableContainerBlockEntity support this in vanilla"
        ).define("allow_loot_chest", false)

        private val GIVE_UI_STICK_AND_MESSAGE_FIRST_LOGIN =
            BUILDER.define("give_ui_stick_and_message_first_login", true)

        private val MONITOR_MAX_DAMAGE = BUILDER.push("consume")
            .defineInRange("monitor_max_damage", 10000, 1, Int.MAX_VALUE)

        private val CRAFTER_MAX_DAMAGE = BUILDER
            .defineInRange("crafter_max_damage", 100000, 1, Int.MAX_VALUE)

        private val PER_OPEN_CONSUME = BUILDER
            .defineInRange("per_open_consume", 10, 0, Int.MAX_VALUE)

        private val PER_TICK_OPEN_CONSUME = BUILDER
            .defineInRange("per_tick_open_consume", 0, 0, Int.MAX_VALUE)

        private val PER_ITEM_TAKE_CONSUME = BUILDER
            .comment("count like bundle, un-stackable item is regarded as 64 normal stack items")
            .defineInRange("per_item_take_consume", 5, 0, Int.MAX_VALUE)

        private val PER_CRAFT_CONSUME = BUILDER
            .comment("count like bundle, un-stackable item is regarded as 64 normal stack items")
            .defineInRange("per_item_craft_consume", 10, 0, Int.MAX_VALUE)

        private val ITEM_POWER_VALUE = BUILDER
            .pop().push("charge")
            .comment("1", "")
            .defineList("item_power_value", listOf("1")) { true }

        private val FUEL_POWER_BY_BURN_TIME = BUILDER
            .comment("burn tick(count in tick) * ration, for ration with zero, will disable")
            .defineInRange("fuel_power_by_burn_time", 5, 0, Int.MAX_VALUE)

        private val POWER_CHARGE_RATE = BUILDER.push("forge_energy_power_rate")
            .comment("measure in tick, -1 for un-limit")
            .defineInRange("power_charge_rate", 200, 0 ,Int.MAX_VALUE)

        private val next = BUILDER.pop()


        private val SPEC: ForgeConfigSpec = BUILDER.build()

        override fun registerEvent(dispatcher: Dispatcher) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SPEC)
            dispatcher<ModConfigEvent> { event -> onLoad(event) }
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
        var globalMode: ListMode = ListMode.IGNORE
            private set
        var giveUIStickAndMessageFirstLogin = true
            private set
        var monitorMaxDamage = 10000
            private set
        var crafterMaxDamage = 100000
            private set
        var perOpenConsume = 10
            private set
        var perTickOpenConsume = 0
            private set
        var perItemTakeConsume = 5
            private set
        var perCrftConsume = 10
            private set
        var itemPowerValue = listOf("1")
            private set
        var fulePowerByBurnTime = 5
            private set
        var powerChargeRate = 200
            private set

        private fun onLoad(event: ModConfigEvent) {
            if (event.config.type != ModConfig.Type.SERVER) return
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

            GlobalFilter.set(GLOBAL_LIST_MODE.get(), GLOBAL_LIST.get().map(Selector::analyze))
            GlobalFilter.setAllowLootChest(ALLOW_LOOT_CHEST.get())

            giveUIStickAndMessageFirstLogin = GIVE_UI_STICK_AND_MESSAGE_FIRST_LOGIN.get()

            monitorMaxDamage = MONITOR_MAX_DAMAGE.get()
            crafterMaxDamage = CRAFTER_MAX_DAMAGE.get()
            perOpenConsume = PER_OPEN_CONSUME.get()
            perTickOpenConsume = PER_TICK_OPEN_CONSUME.get()
            perItemTakeConsume = PER_ITEM_TAKE_CONSUME.get()
            perCrftConsume = PER_CRAFT_CONSUME.get()
            itemPowerValue = ITEM_POWER_VALUE.get()
            fulePowerByBurnTime = FUEL_POWER_BY_BURN_TIME.get()
            powerChargeRate = POWER_CHARGE_RATE.get().let { if (it < 0) Int.MAX_VALUE else it }
        }
    }

    data object Client : BusSubscribe {
        private val BUILDER = ForgeConfigSpec.Builder()

        private val BLUR_TYPE = BUILDER.comment(
            "if this not works when you want ModernUI",
            "you can add com.github.zomb_676.cargo_hologram.ui.CargoBlurScreen to blurBlacklist in its config",
            "https://github.com/BloCamLimb/ModernUI-MC/blob/9a9fdfdd662822b89115b74c365076979c5284aa/forge/src/main/java/icyllis/modernui/mc/Config.java#L268"
        ).defineEnum("blur_type", CargoBlurScreen.BlurType.SELF)
        private val BLUR_RADIUS = BUILDER.push("blur_style").defineInRange("blur_radius", 20.0, 0.1, 100.0)
        private val BLUR_EXPAND_Y = BUILDER.defineInRange("blur_expand_y", 10, 0, Int.MAX_VALUE)
        private val BLUR_BG_ALPHA = BUILDER.defineInRange("blur_bg_alpha", 0x7f, 0, 0xff)
        private val BLUR_OUTLINE = BUILDER.define("blur_outline", true)
        val SEARCH_BACKEND = BUILDER.pop().comment(
            "if set JEI, search will be filtered by jei search result, and fallback to mod if jei is not installed",
            "mod support search tag begin with # and mod name begin with @"
        ).defineEnum("search_backend", SearchEngine.Type.JEI)

        private val TRANSFORM_WHITELIST: ForgeConfigSpec.ConfigValue<MutableList<out String>> =
            BUILDER.push("transform button").comment("support MenuType(don't skip namespace part) and Class name")
                .defineList(
                    "transform_white_list_class", listOf("net.minecraft.client.gui.screens.inventory.InventoryScreen")
                ) { true }

        private val SPEC: ForgeConfigSpec = BUILDER.build()

        override fun registerEvent(dispatcher: Dispatcher) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC)
            dispatcher<ModConfigEvent> { event -> onLoad(event) }
        }

        var blurType = CargoBlurScreen.BlurType.SELF
            private set

        private fun onLoad(event: ModConfigEvent) {
            if (event.config.type != ModConfig.Type.CLIENT) return
            this.blurType = BLUR_TYPE.get()
            BlurConfigure.blurRadius = BLUR_RADIUS.get().toFloat()
            BlurConfigure.blurExpandY = BLUR_EXPAND_Y.get()
            BlurConfigure.blurBgAlpha = BLUR_BG_ALPHA.get()
            BlurConfigure.blurOutline = BLUR_OUTLINE.get()
            SearchEngine.setBacked(SEARCH_BACKEND.get())

            FavouriteItemsEventHandle.loadTransformButtonWhiteList(TRANSFORM_WHITELIST.get())
        }

        fun saveBlurConfigure() {
            BLUR_RADIUS.set(BlurConfigure.blurRadius.toDouble())
            BLUR_EXPAND_Y.set(BlurConfigure.blurExpandY)
            BLUR_BG_ALPHA.set(BlurConfigure.blurBgAlpha)
            BLUR_OUTLINE.set(BlurConfigure.blurOutline)
        }
    }

    override fun registerEvent(dispatcher: Dispatcher) {
        Server.dispatch()
        runOnDistClient { { Client.dispatch() } }
    }
}
