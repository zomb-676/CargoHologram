package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.data.CargoHologramDataGenerator
import com.github.zomb_676.cargo_hologram.network.NetworkHandle
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import com.github.zomb_676.cargo_hologram.trace.monitor.MonitorCenter
import com.github.zomb_676.cargo_hologram.ui.*
import com.github.zomb_676.cargo_hologram.ui.component.CargoHologramComponents
import com.github.zomb_676.cargo_hologram.util.*
import com.mojang.logging.LogUtils
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import net.minecraftforge.client.event.RegisterNamedRenderTypesEvent
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import org.slf4j.Logger

@Mod(CargoHologram.MOD_ID)
class CargoHologram {
    companion object {
        const val MOD_ID = "cargo_hologram"
        const val MOD_NAME = "CargoHologram"
        val LOGGER: Logger = LogUtils.getLogger()

        fun rl(path: String) = ResourceLocation(MOD_ID, path)
    }

    lateinit var textures: CargoHologramSpriteUploader

    init {
        arrayOf(
            Config,
            AllRegisters,
            QueryCenter,
            MonitorCenter,
            AllCommands,
            CargoHologramDataGenerator,
            CapRegisters
        ).dispatch()
        runOnDistClient {
            {
                arrayOf(
                    DebugHud,
                    PanelHud,
                    CargoStorageHud,
                    ClientResultCache,
                    CargoHologramComponents,
                    BlurHandle
                ).dispatch()
            }
        }
        NetworkHandle.registerPackets()

        Dispatcher.enqueueWork<FMLClientSetupEvent> {
            MenuScreens.register(AllRegisters.Menus.CRAFTER_MANU.get(), ::CraftScreen)
            MenuScreens.register(AllRegisters.Menus.FILTER_MANU.get(), ::FilterScreen)
            MenuScreens.register(AllRegisters.Menus.CARGO_STORAGE_MENU.get(), ::CargoStorageScreen)
            MenuScreens.register(AllRegisters.Menus.INSERTER_MENU.get(), ::InserterScreen)
        }

        Dispatcher<RegisterClientReloadListenersEvent> { event ->
            textures = CargoHologramSpriteUploader(currentMinecraft().textureManager)
            event.registerReloadListener(textures)
        }

        runOnDistClient {
            {
                Dispatcher<RenderGuiOverlayEvent.Pre> { event ->
                    if (event.overlay == VanillaGuiOverlay.CROSSHAIR.type() && currentMinecraft().screen is CargoBlurScreen) {
                        event.isCanceled = true
                    }
                }
            }
        }
    }
}