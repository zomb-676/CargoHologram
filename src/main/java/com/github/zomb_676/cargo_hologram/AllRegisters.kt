package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.CargoHologram.Companion.MOD_ID
import com.github.zomb_676.cargo_hologram.item.CargoFilter
import com.github.zomb_676.cargo_hologram.item.CargoMonitor
import com.github.zomb_676.cargo_hologram.item.CraftPanel
import com.github.zomb_676.cargo_hologram.item.MonitorGlasses
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import com.github.zomb_676.cargo_hologram.util.MinecraftItems
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.eventbus.api.IEventBus
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
            sequenceOf(Items.monitor, Items.crafter, Items.filter, Items.glasses)
                .map(RegistryObject<out Item>::get)
                .forEach(event::accept)
        }
        //manually init class
        Items.monitor
    }

    val CREATIVE_TAB = TAB.register("cargo") {
        CreativeModeTab.builder()
            .icon { ItemStack(MinecraftItems.DIAMOND) }
            .build()
    }

    val CRAFTER_MANU: RegistryObject<MenuType<CraftMenu>> = MENU.register("crafter") {
        MenuType(::CraftMenu, FeatureFlags.DEFAULT_FLAGS)
    }

    object Items {
        val monitor: RegistryObject<CargoMonitor> = ITEM.register("monitor") { CargoMonitor() }
        val crafter: RegistryObject<CraftPanel> = ITEM.register("craft_panel") { CraftPanel() }
        val filter: RegistryObject<CargoFilter> = ITEM.register("filter") { CargoFilter() }
        val glasses: RegistryObject<MonitorGlasses> = ITEM.register("monitor_glasses") { MonitorGlasses() }
    }

}