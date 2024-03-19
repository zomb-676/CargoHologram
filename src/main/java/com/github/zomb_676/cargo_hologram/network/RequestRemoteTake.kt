package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.ConsumeEnergyEventHandle
import com.github.zomb_676.cargo_hologram.util.SlotItemStack
import com.github.zomb_676.cargo_hologram.util.dim
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.network.NetworkEvent
import java.util.*

class RequestRemoteTake(
    val takeCount: Int,
    val slotItem: SlotItemStack,
    val pos: BlockPos,
    val level: ResourceKey<Level>,
    val responseKey: UUID,
) :
    NetworkPack<RequestRemoteTake> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): RequestRemoteTake {
            val takeCount = buffer.readInt()
            val slotItemStack = SlotItemStack.decode(buffer)
            val pos = buffer.readBlockPos()
            val level = buffer.readResourceKey(Registries.DIMENSION)
            val key = buffer.readUUID()
            return RequestRemoteTake(takeCount, slotItemStack, pos, level, key)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeInt(takeCount)
        slotItem.encode(buffer)
        buffer.writeBlockPos(pos)
        buffer.writeResourceKey(level)
        buffer.writeUUID(responseKey)
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            var success = false
            val blockEntity = level.dim().getBlockEntity(pos)
            blockEntity?.getCapability(ForgeCapabilities.ITEM_HANDLER)?.ifPresent { handle ->
                val takeSlot = slotItem.slot
                if (handle.slots >= takeSlot) {
                    val item = handle.getStackInSlot(takeSlot)
                    if (!item.isEmpty) {
                        if (item.equals(slotItem.itemStack, false)) {
                            val extractItem = handle.extractItem(takeSlot, takeCount, false)
                            ConsumeEnergyEventHandle.takeConsume(extractItem, context.sender!!)
                            giveOrThrow(context.sender!!, extractItem)
                            success = true
                        }
                    }
                }
            }
            ResponsePack.of(success, responseKey).sendToPlayer(context.sender!!)
        }
    }

    private fun giveOrThrow(player: ServerPlayer, itemStack: ItemStack) {
        val addSuccess = player.addItem(itemStack)
        if (addSuccess && itemStack.isEmpty) {
            player.level().playSound(
                null, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2f,
                ((player.random.nextFloat() - player.random.nextFloat()) * 0.7f + 1.0f) * 2.0f
            )
        } else {
            val itemEntity = player.drop(itemStack, false)
            if (itemEntity != null) {
                itemEntity.setNoPickUpDelay()
                itemEntity.setTarget(player.uuid)
            }
        }
    }
}