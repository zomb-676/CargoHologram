package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.trace.data.SingleRawResult
import com.github.zomb_676.cargo_hologram.util.log
import com.github.zomb_676.cargo_hologram.util.onDev
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
    private val directions: MutableMap<Class<*>, NetworkDirection> = mutableMapOf()

    fun sendToServer(packet: NetworkPack<*>) = channel.sendToServer(packet)
    fun <T> send(packet: NetworkPack<T>, connection: Connection) =
        channel.sendTo(packet, connection, directions[packet::class.java])

    fun sendTo(packet: NetworkPack<*>, packetTarget: PacketDistributor.PacketTarget) =
        channel.send(packetTarget, packet)

    @Suppress("INACCESSIBLE_TYPE")
    private fun <T> registerEntry(entry: MessageEntry<T>) {
        channel.registerMessage(
            entry.index, entry.type, entry::encode, entry::decode, entry::handle, entry.direction().optional()
        )
    }

    /**
     * further warp
     *
     * [FriendlyByteBuf]'s first byte is used to store [MessageEntry.index]
     *
     * * direction assert if specified
     * * [NetworkEvent.Context.packetHandled] set true
     * * [FriendlyByteBuf.writerIndex] and [FriendlyByteBuf.readerIndex] check
     * at [MessageEntry.decode] and [MessageEntry.encode]
     */
    private inline fun <reified T : NetworkPack<T>> register(
        crossinline decodeFunction: (FriendlyByteBuf) -> T,
        direction: NetworkDirection? = null,
    ) {
        registerEntry(object : MessageEntry<T>(T::class.java) {
            override fun encode(instance: T, buf: FriendlyByteBuf) {
                instance.encode(buf)
                if (buf.readerIndex() != 0) {
                    //don't read while encoding
                    val message = "read must be zero after encode in ${T::class.java.simpleName}"
                    log { error(message) }
                    onDev { throw RuntimeException(message) }
                }
            }

            override fun decode(buf: FriendlyByteBuf): T {
                if (buf.readerIndex() != 1) {
                    //the readerIndex 0 is used to identify pack type by forge automatically
                    val message = "readerIndex must be 1 before decode in ${T::class.java.simpleName}"
                    log { error(message) }
                    onDev { throw RuntimeException(message) }
                }
                val res = decodeFunction.invoke(buf)
                if (buf.writerIndex() != buf.readerIndex()) {
                    //not fully read, in most case decode result is wrong
                    //don't write while decoding
                    val message = "writerIndex must equal to readerIndex after decode in ${T::class.java.simpleName}"
                    log { error(message) }
                    onDev { throw RuntimeException(message) }
                }
                return res
            }

            override fun handle(instance: T, context: Supplier<NetworkEvent.Context>) {
                @Suppress("NAME_SHADOWING") val context = context.get()
                if (direction != null) {
                    Asserts.check(
                        context.direction == direction, "register direction:$direction, actual:${context.direction}"
                    )
                }
                try {
                    instance.handle(context)
                } catch (e: Exception) {
                    val message = "error while handle packet for ${T::class.simpleName}"
                    log { error(message, e) }
                    onDev { throw RuntimeException(message, e) }
                }
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
        register(TransformRecipePack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(SetSlotPacket::decode, NetworkDirection.PLAY_TO_SERVER)
        register(SetFilterPack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(InserterTransformPacket::decode, NetworkDirection.PLAY_TO_SERVER)
        register(SetPriorityPack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(SingleRawResult::decode, NetworkDirection.PLAY_TO_CLIENT)
        register(SetBlockPreferPacket::decode, NetworkDirection.PLAY_TO_SERVER)
        register(SetFavouritePack::decode, NetworkDirection.PLAY_TO_SERVER)
        register(TransformPlayerInvToNearbyPack::decode, NetworkDirection.PLAY_TO_SERVER)
    }
}