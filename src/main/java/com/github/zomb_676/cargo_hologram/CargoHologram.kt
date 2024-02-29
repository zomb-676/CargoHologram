package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.network.NetworkHandle
import com.github.zomb_676.cargo_hologram.trace.ClientResultCache
import com.github.zomb_676.cargo_hologram.trace.MonitorCenter
import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import com.mojang.logging.LogUtils
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.Logger

@Mod(CargoHologram.MOD_ID)
class CargoHologram {
    companion object {
        const val MOD_ID = "cargo_hologram"
        const val MOD_NAME = "CargoHologram"
        val LOGGER: Logger = LogUtils.getLogger()

        fun rl(path: String) = ResourceLocation(MOD_ID, path)
    }

    init {
        val modBus: IEventBus = FMLJavaModLoadingContext.get().modEventBus
        val forgeBus: IEventBus = MinecraftForge.EVENT_BUS

        arrayOf(Config, AllRegisters, QueryCenter, MonitorCenter, ClientResultCache, AllCommands).forEach { subscribe ->
            subscribe.registerEvent(modBus, forgeBus)
        }

        NetworkHandle.registerPackets()
    }
}