package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.blockEntity.RemoteCraftTableBlockEntity
import com.github.zomb_676.cargo_hologram.util.dim
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.network.NetworkEvent

class SetBlockPreferPacket(val distance: Int, val pos: BlockPos, val dim: ResourceKey<Level>) :
    NetworkPack<SetBlockPreferPacket> {

    companion object {
        fun decode(buffer: FriendlyByteBuf): SetBlockPreferPacket {
            val distance = buffer.readVarInt()
            val pos = buffer.readBlockPos()
            val dim = buffer.readResourceKey(Registries.DIMENSION)
            return SetBlockPreferPacket(distance, pos, dim)
        }
    }

    constructor(blockEntity: BlockEntity, distance: Int) : this(
        distance,
        blockEntity.blockPos,
        blockEntity.level!!.dimension()
    )

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeVarInt(distance)
        buffer.writeBlockPos(pos)
        buffer.writeResourceKey(dim)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            when (val blockEntity = dim.dim().getBlockEntity(pos)) {
                is RemoteCraftTableBlockEntity -> {
                    blockEntity.monitorDistance = distance
                }

                else -> {}
            }
        }
    }
}