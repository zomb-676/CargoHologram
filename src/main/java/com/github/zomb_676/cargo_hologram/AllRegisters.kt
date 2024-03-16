package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.CargoHologram.Companion.MOD_ID
import com.github.zomb_676.cargo_hologram.item.*
import com.github.zomb_676.cargo_hologram.store.CargoInserter
import com.github.zomb_676.cargo_hologram.store.CargoStorage
import com.github.zomb_676.cargo_hologram.store.blockEntity.CargoStorageBlockEntity
import com.github.zomb_676.cargo_hologram.store.blockEntity.InserterBlockEntity
import com.github.zomb_676.cargo_hologram.ui.*
import com.github.zomb_676.cargo_hologram.util.*
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent
import net.minecraftforge.network.IContainerFactory
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject

object AllRegisters : BusSubscribe {

    private val ITEM: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
    private val BLOCK: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)
    private val MENU: DeferredRegister<MenuType<*>> = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID)
    private val TAB: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID)
    private val BLOCK_ENTITY: DeferredRegister<BlockEntityType<*>> =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MOD_ID)

    override fun registerEvent(dispatcher: Dispatcher) {
        arrayOf(ITEM, BLOCK, MENU, TAB, BLOCK_ENTITY).forEach(dispatcher::registerDeferred)
        dispatcher<BuildCreativeModeTabContentsEvent> { event ->
            if (event.tab != CREATIVE_TAB.get()) return@dispatcher
            ITEM.entries.asSequence().map(RegistryObject<out Item>::get).forEach(event::accept)
        }
        //manually init class
        Items.monitor
        Blocks.remoteCraftTable
        BlockEntities.cargoStorage
        Menus.FILTER_MANU
    }

    val CREATIVE_TAB: RegistryObject<CreativeModeTab> = TAB.register("cargo") {
        CreativeModeTab.builder().title(AllTranslates.MOD_TAB).icon { ItemStack(MinecraftItems.BARREL) }.build()
    }

    object Menus {
        val CRAFTER_MANU: RegistryObject<MenuType<CraftMenu>> = MENU.register("crafter") {
            MenuType(::CraftMenu, FeatureFlags.DEFAULT_FLAGS)
        }
        val FILTER_MANU: RegistryObject<MenuType<FilterMenu>> = MENU.register("filter") {
            MenuType(::FilterMenu, FeatureFlags.DEFAULT_FLAGS)
        }
        val CARGO_STORAGE_MENU: RegistryObject<MenuType<CargoStorageMenu>> = MENU.register("cargo_storage") {
            MenuType(object : IContainerFactory<CargoStorageMenu> {
                override fun create(windowId: Int, inv: Inventory, data: FriendlyByteBuf): CargoStorageMenu =
                    CargoStorageMenu(windowId, inv, data.readBlockPos())
            }, FeatureFlags.DEFAULT_FLAGS)
        }
        val INSERTER_MENU: RegistryObject<MenuType<InserterMenu>> = MENU.register("inserter") {
            MenuType(object : IContainerFactory<InserterMenu> {
                override fun create(windowId: Int, inv: Inventory, data: FriendlyByteBuf): InserterMenu =
                    InserterMenu(windowId, inv, data.readBlockPos())
            }, FeatureFlags.DEFAULT_FLAGS)
        }
    }


    object Blocks {
        val remoteCraftTable: RegistryObject<RemoteCraftTable> =
            BLOCK.register("remote_craft_table") { RemoteCraftTable() }
        val cargoStorage: RegistryObject<CargoStorage> = BLOCK.register("cargo_storage") { CargoStorage() }
        val cargoInserter: RegistryObject<CargoInserter> = BLOCK.register("cargo_inserter") { CargoInserter() }
    }

    object Items {
        val monitor: RegistryObject<CargoMonitor> = ITEM.register("monitor") { CargoMonitor() }
        val crafter: RegistryObject<CraftPanel> = ITEM.register("craft_panel") { CraftPanel() }
        val cargoFilter: RegistryObject<CargoFilter> = ITEM.register("cargo_filter") { CargoFilter() }
        val panel: RegistryObject<MonitorPanel> = ITEM.register("monitor_glasses") { MonitorPanel() }
        val traitFilter: RegistryObject<TraitFilterItem> = ITEM.register("trait_filter") { TraitFilterItem() }
        val listFilter: RegistryObject<ListFilterItem> = ITEM.register("list_filter") { ListFilterItem() }
        val configureUIStick: RegistryObject<Item> =
            ITEM.register("configure_ui_stick") { UIConfigureItem { ::ConfigureScreen } }
        val remoteCraftTableItem: RegistryObject<BlockItem> = ITEM.register("remote_craft_table") {
            BlockItem(Blocks.remoteCraftTable.get(), Item.Properties())
        }
        val cargoStorageItem: RegistryObject<BlockItem> = ITEM.register("cargo_storage") {
            BlockItem(Blocks.cargoStorage.get(), Item.Properties())
        }
        val cargoInserter: RegistryObject<BlockItem> = ITEM.register("cargo_inserter") {
            BlockItem(Blocks.cargoInserter.get(), Item.Properties())
        }
        val linker: RegistryObject<LinkerItem> = ITEM.register("linker") { LinkerItem() }
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    object BlockEntities {
        val cargoStorage = BLOCK_ENTITY.register("cargo_storage") {
            BlockEntityType.Builder.of(::CargoStorageBlockEntity, Blocks.cargoStorage.get()).build(null)
        }
        val inserter = BLOCK_ENTITY.register("cargo_inserter") {
            BlockEntityType.Builder.of(::InserterBlockEntity, Blocks.cargoInserter.get()).build(null)
        }
    }

}