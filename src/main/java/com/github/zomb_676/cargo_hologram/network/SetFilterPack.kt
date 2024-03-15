package com.github.zomb_676.cargo_hologram.network

import com.github.zomb_676.cargo_hologram.ui.FilterMenu
import com.github.zomb_676.cargo_hologram.util.filter.ItemTrait
import com.github.zomb_676.cargo_hologram.util.filter.SpecifiedItemTrait
import com.github.zomb_676.cargo_hologram.util.filter.TraitList
import com.github.zomb_676.cargo_hologram.util.optional
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.network.NetworkEvent
import java.util.*

class SetFilterPack(private val trait: Optional<TraitList>) : NetworkPack<SetFilterPack> {
    constructor(trait: TraitList) : this(trait.optional())

    companion object {
        fun decode(buffer: FriendlyByteBuf): SetFilterPack {
            val trait = buffer.readOptional { buffer ->
                val tag = buffer.readNbt()!!
                val trait = TraitList()
                trait.deserializeNBT(tag)
                trait
            }
            return SetFilterPack(trait)
        }
    }

    override fun encode(buffer: FriendlyByteBuf) {
        buffer.writeOptional(trait) { buffer, trait ->
            buffer.writeNbt(trait.serializeNBT())
        }
    }

    override fun handle(context: NetworkEvent.Context) {
        context.enqueueWork {
            when (val menu = context.sender!!.containerMenu) {
                is FilterMenu -> {
                    trait.ifPresentOrElse({ t -> t.writeToItem(menu.playerInv.getSelected()) }, {
                        TraitList.removeTag(menu.playerInv.getSelected())
                    })
                }
            }
        }
    }
}