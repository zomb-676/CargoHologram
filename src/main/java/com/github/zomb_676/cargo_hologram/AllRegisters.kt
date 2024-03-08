package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.CargoHologram.Companion.MOD_ID
import com.github.zomb_676.cargo_hologram.item.*
import com.github.zomb_676.cargo_hologram.ui.ConfigureScreen
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.ui.FilterMenu
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.MutableComponent
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object AllRegisters : BusSubscribe {

    private val ITEM: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
    private val BLOCK: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)
    private val MENU: DeferredRegister<MenuType<*>> = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID)
    private val TAB: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)

    override fun registerEvent(dispatcher: Dispatcher) {
        arrayOf(ITEM, BLOCK, MENU, TAB).forEach(dispatcher::registerDeferred)
        dispatcher<BuildCreativeModeTabContentsEvent> { event ->
            if (event.tab != CREATIVE_TAB.get()) return@dispatcher
            ITEM.entries.asSequence()
                .map(RegistryObject<out Item>::get)
                .forEach(event::accept)
        }
        //manually init class
        Items.monitor
    }

    val tabName: MutableComponent = "itemGroup.$MOD_ID.items".translate()
    val CREATIVE_TAB: RegistryObject<CreativeModeTab> = TAB.register("cargo") {
        CreativeModeTab.builder()
            .title(tabName)
            .icon { ItemStack(MinecraftItems.DIAMOND) }
            .build()
    }

    val CRAFTER_MANU: RegistryObject<MenuType<CraftMenu>> = MENU.register("crafter") {
        MenuType(::CraftMenu, FeatureFlags.DEFAULT_FLAGS)
    }
    val FILTER_MANU: RegistryObject<MenuType<FilterMenu>> = MENU.register("filter") {
        MenuType(::FilterMenu, FeatureFlags.DEFAULT_FLAGS)
    }


    object Items {
        val monitor: RegistryObject<CargoMonitor> = ITEM.register("monitor") { CargoMonitor() }
        val crafter: RegistryObject<CraftPanel> = ITEM.register("craft_panel") { CraftPanel() }
        val cargoFilter: RegistryObject<CargoFilter> = ITEM.register("cargo_filter") { CargoFilter() }
        val glasses: RegistryObject<MonitorGlasses> = ITEM.register("monitor_glasses") { MonitorGlasses() }
        val itemFilter: RegistryObject<ItemFilter> = ITEM.register("filter_item") { ItemFilter() }
        val configureUISTick: RegistryObject<Item> =
            ITEM.register("configure_ui_sitck") { UIConfigureItem(::ConfigureScreen) }
    }

}