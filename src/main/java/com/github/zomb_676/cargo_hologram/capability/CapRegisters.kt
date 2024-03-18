package com.github.zomb_676.cargo_hologram.capability

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.AllTranslates
import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.*
import net.minecraftforge.common.util.INBTSerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.event.entity.player.PlayerEvent

object CapRegisters : BusSubscribe {

    val PLAYER_FIRST_TAKE: Capability<PlayerFirstTake> =
        CapabilityManager.get(object : CapabilityToken<PlayerFirstTake>() {})
    private val PLAYER_FIRST_TAKE_PATH = CargoHologram.rl("player_first_take")

    val CARGO_ENERGY_ITEM: Capability<CargoEnergyItemCapability> =
        CapabilityManager.get(object : CapabilityToken<CargoEnergyItemCapability>() {})
    private val CARGO_ENERGY_ITEM_PATH = CargoHologram.rl("cargo_energy_item")

    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<RegisterCapabilitiesEvent> { event ->
            event.register(PlayerFirstTake::class.java)
            event.register(CargoEnergyItemCapability::class.java)
        }
        dispatcher<AttachCapabilitiesEvent<Entity>, _> { event ->
            val obj = event.`object`
            if (obj !is Player) return@dispatcher
            ForgeCapabilities.ENERGY
            if (!obj.getCapability(PLAYER_FIRST_TAKE).isPresent) {
                event.addCapability(PLAYER_FIRST_TAKE_PATH, PlayerFirstTakeProvider())
            }
        }
        dispatcher<PlayerEvent.Clone> { event ->
            if (event.isWasDeath) {
                event.original.getCapability(PLAYER_FIRST_TAKE).ifPresent { old ->
                    event.entity.getCapability(PLAYER_FIRST_TAKE).ifPresent { new ->
                        new.copyFrom(old)
                    }
                }
            }
        }
        dispatcher<PlayerEvent.PlayerLoggedInEvent> { event ->
            event.entity.getCapability(PLAYER_FIRST_TAKE).ifPresent { cap ->
                if (!cap.isPlayerFirstTake && Config.Server.giveUIStickAndMessageFirstLogin) {
                    if (event.entity.inventory.add(ItemStack(AllRegisters.Items.configureUIStick.get()))) {
                        cap.isPlayerFirstTake = true
                    }
                    event.entity.sendSystemMessage(AllTranslates.CONFIGURE_UI_TIP)
                }
            }
        }

        dispatcher<AttachCapabilitiesEvent<ItemStack>, _> { event ->
            val obj = event.`object`
            if (obj.item == AllRegisters.Items.monitor.get()) {
                if (!obj.getCapability(CARGO_ENERGY_ITEM).isPresent) {
                    event.addCapability(CARGO_ENERGY_ITEM_PATH, CargoEnergyItemProvider())
                }
            }
        }
    }

    class PlayerFirstTakeProvider : ICapabilityProvider, INBTSerializable<CompoundTag> {
        private val instance = PlayerFirstTake()
        override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> = getCapability(cap)
        override fun <T : Any> getCapability(cap: Capability<T>): LazyOptional<T> =
            if (cap == PLAYER_FIRST_TAKE) {
                LazyOptional.of { instance }.cast()
            } else LazyOptional.empty()

        override fun serializeNBT(): CompoundTag = CompoundTag().apply { instance.saveNBTData(this) }
        override fun deserializeNBT(nbt: CompoundTag) = instance.loadNBTData(nbt)
    }

    class CargoEnergyItemProvider(val instance : CargoEnergyItemCapability) : ICapabilityProvider, INBTSerializable<CompoundTag> {
        constructor() : this(CargoEnergyItemCapability(10000,0))

        override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> = getCapability(cap)
        override fun <T : Any> getCapability(cap: Capability<T>): LazyOptional<T> =
//            if (cap == CARGO_ENERGY_ITEM || cap == ForgeCapabilities.ENERGY) {
            if (cap == ForgeCapabilities.ENERGY) {
                LazyOptional.of { instance }.cast()
            } else LazyOptional.empty()

        override fun serializeNBT(): CompoundTag = instance.serializeNBT()

        override fun deserializeNBT(nbt: CompoundTag) = instance.deserializeNBT(nbt)

    }
}