package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.trace.QueryCenter
import com.github.zomb_676.cargo_hologram.trace.QueryRequirement
import com.github.zomb_676.cargo_hologram.trace.QuerySource
import com.github.zomb_676.cargo_hologram.util.queryPlayer
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import org.apache.http.util.Asserts
import java.util.*

data class PlayerCenteredQueryRequestPack(
    val player: UUID,
    val radius: Int,
    val requirement: QueryRequirement,
) : NetworkPack<PlayerCenteredQueryRequestPack> {

    companion object {
        fun decode(buffer: FriendlyByteBuf): PlayerCenteredQueryRequestPack {
            val player = buffer.readUUID()
            val radius = buffer.readInt()
            val requirement = QueryRequirement.decode(buffer)
            return PlayerCenteredQueryRequestPack(player, radius, requirement)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeUUID(player)
        buffer.writeInt(radius)
        requirement.encode(buffer)
    }

    override fun handle(context: NetworkEvent.Context) {
        Asserts.check(context.sender!!.uuid == this.player, "sender and changed player don't have same UUID")
        context.enqueueWork {
            val playerSource = QuerySource.ofPlayerCentered(player.queryPlayer()!!, radius, requirement)
            QueryCenter.appendSource(playerSource)
        }
    }

}
