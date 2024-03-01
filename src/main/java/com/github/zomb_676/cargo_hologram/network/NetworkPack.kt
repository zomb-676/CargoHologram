package com.github.zomb_676.cargo_hologram.network

import net.minecraft.network.Connection
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.network.PacketDistributor

interface NetworkPack<T> {
    fun encode(buffer: FriendlyByteBuf)

    /**
     * @param context [NetworkEvent.Context.packetHandled] will be set
     */
    fun handle(context: NetworkEvent.Context)

    fun sendToServer() = NetworkHandle.sendToServer(this)
    fun send(connection: Connection) = NetworkHandle.send(this, connection)
    fun sendTo(packetTarget: PacketDistributor.PacketTarget) = NetworkHandle.sendTo(this, packetTarget)
    fun sendToPlayer(player: ServerPlayer) = sendTo(PacketDistributor.PLAYER.with { player })
}