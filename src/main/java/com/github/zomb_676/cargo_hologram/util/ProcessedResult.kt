package com.github.zomb_676.cargo_hologram.util

import com.github.zomb_676.cargo_hologram.trace.data.MonitorRawResult
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemStack
import java.util.function.Predicate

class ProcessedResult private constructor(val data: Map<BlockPos, List<SlotItemStack>>) {

    private var cachedCount = -1

    companion object {
        fun convert(monitorRawResult: MonitorRawResult): ProcessedResult {
            return ProcessedResult(monitorRawResult.data)
        }
    }

    fun iterBy(): Iterator<Map.Entry<BlockPos, List<SlotItemStack>>> {
        return data.iterator()
    }

    fun count(): Int {
        if (cachedCount == -1) {
            cachedCount = data.values.sumOf(List<SlotItemStack>::count)
        }
        return cachedCount
    }

    fun iterBy(str: Predicate<ItemStack>): CountIterator<Pair<BlockPos, Sequence<SlotItemStack>>> {
        val sequence = data.asSequence().map { (pos, entry) ->
            pos to entry.asSequence().filter { (_, item) -> str.test(item) }
        }
        return CountIterator(sequence.iterator())
    }
}