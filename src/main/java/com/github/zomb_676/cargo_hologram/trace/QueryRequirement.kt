package com.github.zomb_676.cargo_hologram.trace

import com.github.zomb_676.cargo_hologram.selector.Selector
import com.github.zomb_676.cargo_hologram.util.inlineAssert
import net.minecraft.network.FriendlyByteBuf

/**
 * generic parameters used for [QuerySource]
 */
class QueryRequirement(val force: Boolean, val crossDimension: Boolean, vararg val selector: Selector) {
    companion object {
        fun decode(buffer: FriendlyByteBuf): QueryRequirement {
            val force = buffer.readBoolean()
            val crossDim = buffer.readBoolean()
            val selectCount = buffer.readInt()
            val selectors: Array<Selector> = Array(selectCount) {
                buffer.readByteArray().run(::String).run(Selector::analyze)
            }
            return QueryRequirement(force, crossDim, *selectors)
        }
    }

    init {
        assertValid()
    }

    fun encode(buffer: FriendlyByteBuf) {
        buffer.writeBoolean(force)
        buffer.writeBoolean(crossDimension)
        buffer.writeInt(selector.size)
        selector.asSequence().map(Selector::decode).map(String::toByteArray).forEach(buffer::writeByteArray)
    }

    private fun assertSlotNoIntersect() = inlineAssert(selector.distinctBy(Selector::type).size == selector.size) {
        "$selector have entry with same BlockEntityType"
    }

    private fun assertValid() {
        assertSlotNoIntersect()
        selector.forEach(Selector::assertSlotSelectorValid)
    }

}
