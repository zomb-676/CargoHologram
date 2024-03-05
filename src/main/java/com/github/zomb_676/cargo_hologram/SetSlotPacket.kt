package com.github.zomb_676.cargo_hologram

import com.github.zomb_676.cargo_hologram.network.NetworkPack
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.util.location
import com.github.zomb_676.cargo_hologram.util.log
import com.github.zomb_676.cargo_hologram.util.logOnDebug
import com.github.zomb_676.cargo_hologram.util.query
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraftforge.network.NetworkEvent
import net.minecraftforge.registries.ForgeRegistries

data class SetSlotPacket(val slot: Int, val item: ItemStack, val menuType: MenuType<*>) : NetworkPack<SetSlotPacket> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): SetSlotPacket {
            val slot = buffer.readInt()
            val item = buffer.readItem()
            val menuType = buffer.readResourceLocation().query(ForgeRegistries.MENU_TYPES)
            return SetSlotPacket(slot, item, menuType)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeInt(slot)
        buffer.writeItemStack(item, false)
        buffer.writeResourceLocation(menuType.location(ForgeRegistries.MENU_TYPES))
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            val sender = context.sender ?: return@enqueueWork
            val containerMenu = sender.containerMenu
            if (containerMenu.type != menuType) {
                logOnDebug { error("client:$menuType,server:${containerMenu.type}") }
                return@enqueueWork
            }
            when (containerMenu) {
                is CraftMenu -> containerMenu.setSlotItem(slot, item)
                else -> log { debug("un-processed SetSlotPacket for menuType:$menuType") }
            }
        }
    }

}