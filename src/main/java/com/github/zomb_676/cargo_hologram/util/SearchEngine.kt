package com.github.zomb_676.cargo_hologram.util

import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.compact.jei.CargoHologramJeiPlugin
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fml.ModList
import java.util.function.Predicate

object SearchEngine {

    private var backedType : Type = Type.JEI

    enum class Type {
        JEI, SELF
    }

    interface SearchBacked {
        var searchText: String
        fun containsInResult(itemStack: ItemStack): Boolean
    }

    object SelfBacked : SearchBacked {
        override var searchText: String = ""
            set(value) {
                if (value != field) {
                    field = value
                    match = update()
                }
            }
        private val ALWAYS_PASS : Predicate<ItemStack> = Predicate { true }
        private var match: Predicate<ItemStack> = ALWAYS_PASS

        private fun update(): Predicate<ItemStack> {
            if (searchText.isEmpty()) return ALWAYS_PASS
            return when (searchText.first()) {
                '@' -> {
                    val regex = Regex(searchText.substring(1), RegexOption.IGNORE_CASE)
                    Predicate { item -> regex.containsMatchIn(item.item.getCreatorModId(item) ?: "") }
                }

                '#' -> {
                    val regex = Regex(searchText.substring(1), RegexOption.IGNORE_CASE)
                    Predicate { item -> item.tags.anyMatch { tag -> regex.containsMatchIn(tag.location.toString()) } }
                }

                else -> {
                    val regex = Regex(searchText, RegexOption.IGNORE_CASE)
                    Predicate { item -> regex.containsMatchIn(item.displayName.string) }
                }
            }
        }

        override fun containsInResult(itemStack: ItemStack): Boolean = match.test(itemStack)

    }

    fun getBacked(): SearchBacked {
        val configPrefer = this.backedType == Type.JEI
        val jeiInstalled = ModList.get().isLoaded("jei")
        return if (configPrefer && jeiInstalled) {
            CargoHologramJeiPlugin.jeiSearchBacked!!
        } else {
            SelfBacked
        }
    }

    fun getBackedType() = this.backedType

    fun setBacked(type: Type) {
        if (type != Type.JEI) {
            val backed = getBacked()
            if (backed != SelfBacked) {
                backed.searchText = ""
            }
        }
        this.backedType = type
    }

    fun save() {
        Config.Client.SEARCH_BACKEND.set(this.backedType)
    }
}