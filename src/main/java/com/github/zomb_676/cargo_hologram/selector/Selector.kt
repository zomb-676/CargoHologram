package com.github.zomb_676.cargo_hologram.selector

import com.github.zomb_676.cargo_hologram.trace.GlobalFilter
import com.github.zomb_676.cargo_hologram.util.ListMode
import com.github.zomb_676.cargo_hologram.util.forEachDiffIndex
import com.github.zomb_676.cargo_hologram.util.log
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.registries.ForgeRegistries
import java.util.function.IntPredicate

class Selector(val type: BlockEntityType<*>, val slotSelectors: List<SlotSelector>) {

    companion object {
        fun checkValid(str: String): Boolean {
            var valid = true
            try {

                val strs = str.replace(Regex("\\s*"), "").split(",")
                if (str.isEmpty()) throw RuntimeException("can't analyze $str")
                val path = try {
                    ResourceLocation(strs[0])
                } catch (e: Exception) {
                    log { error("", RuntimeException("can't convert ${strs[0]} as ResourceLocation")) }
                    valid = false
                }


                strs.asSequence().drop(1).map(SlotSelector::analyze).map { res ->
                    res.fold({ value -> value }, { e -> log { error("", e) } })
                }

            } catch (e: Exception) {
                valid = false
            }
            return valid
        }

        fun analyze(str: String): Selector {
            val strs = str.replace(Regex("\\s*"), "").split(",")
            if (str.isEmpty()) throw RuntimeException("can't analyze $str")
            val path = try {
                ResourceLocation(strs[0])
            } catch (e: Exception) {
                throw RuntimeException("can't convert ${strs[0]} as ResourceLocation")
            }

            val type = ForgeRegistries.BLOCK_ENTITY_TYPES.getValue(path)
                ?: throw RuntimeException("not exist $path for BlockEntityType")

            if (str.length == 1) return Selector(type, listOf())

            val selectors = strs.asSequence().drop(1).map(SlotSelector::analyze).map { res ->
                res.fold({ value -> value }, { e -> log { error(e) };null })
            }.filterNotNull().toList()

            return Selector(type, selectors)
        }

        fun of(type: BlockEntityType<*>, vararg str: String) =
            Selector(type, str.map(SlotSelector::analyze).map(Result<SlotSelector>::getOrThrow))
    }

    fun decode(): String {
        val location = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(type)!!
        if (slotSelectors.isEmpty()) return location.toString()
        return "$location,${slotSelectors.joinToString(separator = ",", transform = SlotSelector::decode)}"
    }

//    override fun test(value: Int): Boolean {
//        if (slotSelectors.isEmpty()) return true
//        for (check in slotSelectors) {
//            if (check.test(value)) return true
//        }
//        return false
//    }

    fun assertSlotSelectorValid() {
        val slotInvalid = slotSelectors.filter { !it.valid() }
        if (slotInvalid.isNotEmpty()) {
            log { error("${decode()} has invalid slot selector") }
            for (slot in slotInvalid) {
                log { error("${slot.decode()} is invalid") }
            }
            throw AssertionError()
        }

        if (slotSelectors.size <= 2) return

        val problems = mutableListOf<Pair<SlotSelector, SlotSelector>>()
        slotSelectors.forEachDiffIndex { selector1, selector2 ->
            val error: Boolean = when (selector1) {
                is SlotSelector.Single -> selector2.test(selector1.slot)
                is SlotSelector.CloseInterval -> (selector1.left..selector1.right).any(selector2::test)
                is SlotSelector.CloseOpenInterval -> (selector1.left..<selector1.right).any(selector2::test)
                is SlotSelector.OpenCloseInterval -> ((selector1.left + 1)..selector1.right).any(selector2::test)
                is SlotSelector.OpenInterval -> ((selector1.left + 1)..<selector1.right).any(selector2::test)
            }
            if (error) {
                problems.add(selector1 to selector2)
            }
        }
        if (problems.isNotEmpty()) {
            log { error("${decode()} have intersect slot selector") }
            problems.forEach { (s1, s2) ->
                log { error("${s1.decode()} intersect ${s2.decode()}") }
            }
            throw AssertionError()
        }
    }

    override fun toString(): String = decode()
    fun asIntPredicate(mode: ListMode): IntPredicate =
        when (mode) {
            ListMode.BLACK_LIST_MODE -> {
                IntPredicate { slot ->
                    if (slotSelectors.isEmpty()) return@IntPredicate true
                    for (selector in slotSelectors) {
                        if (selector.test(slot)) return@IntPredicate false
                    }
                    return@IntPredicate true
                }
            }

            ListMode.WHITE_LIST_MODE -> {
                IntPredicate { slot ->
                    if (slotSelectors.isEmpty()) return@IntPredicate false
                    for (selector in slotSelectors) {
                        if (selector.test(slot)) return@IntPredicate true
                    }
                    return@IntPredicate false
                }
            }

            ListMode.IGNORE -> GlobalFilter.ALWAYS_TRUE
        }
}