package com.github.zomb_676.cargo_hologram.util

import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.GenericEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.IModBusEvent
import org.apache.http.util.Asserts

data class Dispatcher(val modBus: IEventBus, val forgeBus: IEventBus) {

    inline operator fun <reified T : GenericEvent<out V>, reified V : Any> invoke(crossinline codeBlock: (T) -> Unit) {
        Asserts.check(T::class.java.isGenericEvent(), "${T::class.java.simpleName} is GenericEvent<T>")
        switchEvent<T>().addGenericListener<T, V>(V::class.java) { e -> codeBlock(e) }
    }

    inline operator fun <reified T : Event> invoke(crossinline codeBlock: (T) -> Unit) {
        Asserts.check(!T::class.java.isGenericEvent(), "${T::class.java.simpleName} is GenericEvent<T>")
        switchEvent<T>().addListener<T> { e -> codeBlock.invoke(e) }
    }


    fun <T> Class<T>.isGenericEvent() = this.isAssignableFrom(GenericEvent::class.java)
    fun <T> Class<T>.isModEvent() = this.isAssignableFrom(IModBusEvent::class.java)

    inline fun <reified T : Event> switchEvent(): IEventBus {
        return if (T::class.java.isModEvent()) {
            modBus
        } else {
            forgeBus
        }
    }
}