package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.item.ItemFilter
import com.github.zomb_676.cargo_hologram.ui.FilterMenu
import com.github.zomb_676.cargo_hologram.util.filter.ItemTrait
import com.github.zomb_676.cargo_hologram.util.optional
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.*

class SetFilterPack(private val trait: Optional<ItemTrait>) : NetworkPack<SetFilterPack> {
    constructor(trait: ItemTrait) : this(trait.optional())

    companion object {
        fun decode(buffer: FriendlyByteBuf): SetFilterPack {
            val trait = buffer.readOptional { buffer ->
                val tag = buffer.readNbt()!!
                ItemTrait.readItemTrait(tag)
            }
            return SetFilterPack(trait)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeOptional(trait) { buffer, trait ->
            val tag = CompoundTag()
            trait.writeToNbt(tag)
            buffer.writeNbt(tag)
        }
    }

    override fun handle(context: NetworkEvent.Context) {
        when (val menu = context.sender!!.containerMenu) {
            is FilterMenu -> {
                trait.ifPresentOrElse({ t -> t.writeToItemNbt(menu.filterItem) }, {
                    ItemTrait.removeTag(menu.filterItem)
                })
            }
        }
    }
}