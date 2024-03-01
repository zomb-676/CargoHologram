package com.github.zomb_676.cargo_hologram.util

import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.ui.CommandDSL
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.inventory.tooltip.TooltipComponent
import net.minecraftforge.client.event.RegisterClientCommandsEvent
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.RegisterCommandsEvent
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.IModBusEvent
import net.minecraftforge.fml.event.lifecycle.ParallelDispatchEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import org.apache.http.util.Asserts
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicReference

object Dispatcher {
    val modBus get() = FMLJavaModLoadingContext.get().modEventBus!!
    val forgeBus get() = MinecraftForge.EVENT_BUS!!

    @JvmName("registerForGenericEvent")
    inline operator fun <reified T : GenericEvent<out V>, reified V : Any> invoke(crossinline codeBlock: (T) -> Unit) {
        invoke<T, V>(EventPriority.NORMAL, false, codeBlock)
    }

    @JvmName("registerForGenericEvent")
    inline operator fun <reified T : GenericEvent<out V>, reified V : Any> invoke(
        priority: EventPriority = EventPriority.NORMAL,
        receiveCanceled: Boolean = false,
        crossinline codeBlock: (T) -> Unit,
    ) {
        Asserts.check(T::class.java.isGenericEvent(), "${T::class.java.simpleName} is not GenericEvent<T>")
        switchEvent<T>().addGenericListener(
            V::class.java, priority, receiveCanceled, T::class.java
        ) { e -> codeBlock(e) }
    }

    @JvmName("registerForNoneGenericEvent")
    inline operator fun <reified T : Event> invoke(crossinline codeBlock: (T) -> Unit) {
        invoke<T>(EventPriority.NORMAL, false, codeBlock)
    }

    @JvmName("registerForNoneGenericEvent")
    inline operator fun <reified T : Event> invoke(
        priority: EventPriority = EventPriority.NORMAL,
        receiveCanceled: Boolean = false,
        crossinline codeBlock: (T) -> Unit,
    ) {
        Asserts.check(!T::class.java.isGenericEvent(), "${T::class.java.simpleName} is GenericEvent<T>")
        switchEvent<T>().addListener(priority, receiveCanceled, T::class.java) { e ->
            try {
                codeBlock.invoke(e)
            } catch (e : Exception) {
                CargoHologram.LOGGER.error("error while run event")
                throw e
            }
        }
    }

    fun <T : DeferredRegister<*>> registerDeferred(register: T) {
        register.register(modBus)
    }

    inline fun registerCommand(crossinline f: (CommandDSL<CommandSourceStack>).() -> Unit) {
        invoke<RegisterCommandsEvent> { event ->
            CommandDSL(event.dispatcher).apply {
                f(this)
            }
        }
    }

    inline fun registerClientCommand(crossinline f: (CommandDSL<CommandSourceStack>).() -> Unit) {
        invoke<RegisterClientCommandsEvent> { event ->
            CommandDSL(event.dispatcher).apply {
                f(this)
            }
        }
    }

    inline fun <reified T : TooltipComponent, V : ClientTooltipComponent> registerTooltipComponent(noinline f: (T) -> V) {
        invoke<_> { event: RegisterClientTooltipComponentFactoriesEvent ->
            event.register(T::class.java, f)
        }
    }

    @JvmName("enqueueWithVoid")
    inline fun <reified T : ParallelDispatchEvent> enqueueWork(
        priority: EventPriority = EventPriority.NORMAL,
        noinline code: () -> Unit,
    ): () -> CompletableFuture<Void>? {
        val property = AtomicReference<CompletableFuture<Void>?>(null)
        modBus.addListener(priority, false, T::class.java) { event ->
            property.set(event.enqueueWork(code))
        }
        return property::get
    }

    @JvmName("enqueueNoneVoid")
    inline fun <reified T : ParallelDispatchEvent, R> enqueueWork(
        priority: EventPriority = EventPriority.NORMAL,
        noinline code: () -> R,
    ): () -> CompletableFuture<R>? {
        val property = AtomicReference<CompletableFuture<R>?>(null)
        modBus.addListener(priority, false, T::class.java) { event ->
            property.set(event.enqueueWork(code))
        }
        return property::get
    }

    @PublishedApi
    internal fun <T> Class<T>.isGenericEvent() = GenericEvent::class.java.isAssignableFrom(this)

    @PublishedApi
    internal fun <T> Class<T>.isModEvent() = IModBusEvent::class.java.isAssignableFrom(this)

    @PublishedApi
    internal inline fun <reified T : Event> switchEvent(): IEventBus {
        return when {
            T::class.java.isModEvent() -> modBus
            else -> forgeBus
        }
    }
}