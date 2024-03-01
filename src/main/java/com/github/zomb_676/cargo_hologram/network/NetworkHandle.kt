package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.util.optional
import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.NetworkRegistry
import net.minecraftforge.network.PacketDistributor
import org.apache.http.util.Asserts
import java.util.function.Supplier

object NetworkHandle {

    private const val VERSION = "1"

    private val channel = NetworkRegistry.newSimpleChannel(
        CargoHologram.rl("network"), { VERSION }, VERSION::equals, VERSION::equals
    )
    private val directions: MutableMap<Class<*>, NetworkDirection?> = mutableMapOf()

    fun sendToServer(packet: NetworkPack<*>) = channel.sendToServer(packet)
    fun <T> send(packet: NetworkPack<T>, connection: Connection) =
        channel.sendTo(packet, connection, directions[packet::class.java])

    fun sendTo(packet: NetworkPack<*>, packetTarget: PacketDistributor.PacketTarget) =
        channel.send(packetTarget, packet)

    @Suppress("INACCESSIBLE_TYPE")
    private fun <T> register(entry: MessageEntry<T>) {
        channel.registerMessage(
            entry.index, entry.type, entry::encode, entry::decode, entry::handle, entry.direction().optional()
        )
    }

    /**
     * further warp, with direction assert if specified, and [NetworkEvent.Context.packetHandled] set
     */
    private inline fun <reified T : NetworkPack<T>> register(
        crossinline decodeFunction: (FriendlyByteBuf) -> T,
        direction: NetworkDirection? = null,
    ) {
        register(object : MessageEntry<T>(T::class.java) {
            override fun encode(instance: T, buf: FriendlyByteBuf) = instance.encode(buf)
            override fun decode(buf: FriendlyByteBuf): T = decodeFunction.invoke(buf)
            override fun handle(instance: T, context: Supplier<NetworkEvent.Context>) {
                @Suppress("NAME_SHADOWING") val context = context.get()
                if (direction != null) {
                    Asserts.check(
                        context.direction == direction, "register direction:$direction, actual:${context.direction}"
                    )
                }
                instance.handle(context)
                context.packetHandled = true
            }

            override fun direction(): NetworkDirection? = direction
        })
        if (direction != null) {
            directions[T::class.java] = direction
        }
    }

    fun registerPackets() {
        register(PlayerCenteredQueryRequestPack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(PlayerCenteredQueryStopPack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(WrappedResult::decode, NetworkDirection.PLAY_TO_CLIENT)
        register(RequestRemoteTake::decode, NetworkDirection.PLAY_TO_SERVER)
        register(ResponsePack::decode, NetworkDirection.PLAY_TO_CLIENT)
    }
}