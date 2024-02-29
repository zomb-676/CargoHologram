package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.entity.player.Player
import net.minecraftforge.network.NetworkEvent
import java.util.*

data class PlayerCenteredQueryStopPack(val player: UUID) : NetworkPack<PlayerCenteredQueryStopPack> {

    constructor(player : Player) : this(player.uuid)

    companion object {
        fun decode(buffer: FriendlyByteBuf): PlayerCenteredQueryStopPack =
            PlayerCenteredQueryStopPack(buffer.readUUID())
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeUUID(player)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            QueryCenter.stopPlayer(player)
        }
    }
}