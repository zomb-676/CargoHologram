package com.github.zomb_676.cargo_hologram.network

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
import java.util.*

class TransformRecipePack(
    val input: List<Optional<ItemStack>>,
    val output: List<Optional<ItemStack>>,
    val doTransform: Boolean,
    val maxTransform: Boolean,
    val menuType: MenuType<*>,
) : NetworkPack<TransformRecipePack> {
    companion object {
        fun decode(buffer: FriendlyByteBuf): TransformRecipePack {
            val input = List(buffer.readInt()) {
                buffer.readOptional(FriendlyByteBuf::readItem)
            }
            val output = List(buffer.readInt()) {
                buffer.readOptional(FriendlyByteBuf::readItem)
            }
            val doTransform = buffer.readBoolean()
            val maxTransform = buffer.readBoolean()
            val menuType = buffer.readResourceLocation().query(ForgeRegistries.MENU_TYPES)
            return TransformRecipePack(input, output, doTransform, maxTransform, menuType)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeInt(input.size)
        input.forEach { i ->
            buffer.writeOptional(i) { buffer, itemStack ->
                buffer.writeItemStack(itemStack, false)
            }
        }
        buffer.writeInt(output.size)
        output.forEach { i ->
            buffer.writeOptional(i) { buffer, itemStack ->
                buffer.writeItemStack(itemStack, false)
            }
        }
        buffer.writeBoolean(doTransform)
        buffer.writeBoolean(maxTransform)
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
                is CraftMenu -> containerMenu.setRecipe(output, input)
                else -> log { debug("un-processed TransformRecipePack for menuType:$menuType") }
            }
        }
    }

}