package com.github.zomb_676.cargo_hologram.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.*

class ResponsePack private constructor(val success: Boolean, val responseKey: UUID) : NetworkPack<ResponsePack> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): ResponsePack {
            val success = buffer.readBoolean()
            val responseKey = buffer.readUUID()
            return ResponsePack(success, responseKey)
        }

        fun ofSuccess(responseKey: UUID) = ResponsePack(true, responseKey)
        fun ofFailure(responseKey: UUID) = ResponsePack(false, responseKey)
        fun of(success: Boolean, responseKey: UUID) = ResponsePack(success, responseKey)
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBoolean(success)
        buffer.writeUUID(responseKey)
    }

    override fun handle(context: NetworkEvent.Context) {

    }
}