package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.ui.InserterMenu
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent

class InserterTransformPacket : NetworkPack<InserterTransformPacket> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): InserterTransformPacket = InserterTransformPacket()
    }

    override fun encode(buffer: FriendlyByteBuf) {

    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            (context.sender?.containerMenu as? InserterMenu?)?.transform()
        }
    }
}