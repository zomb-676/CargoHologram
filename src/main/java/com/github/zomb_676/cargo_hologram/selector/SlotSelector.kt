package com.github.zomb_676.cargo_hologram.selector

import org.apache.http.util.Asserts
import java.util.function.IntPredicate

@Suppress("ConvertTwoComparisonsToRangeCheck")
sealed interface SlotSelector : IntPredicate {

    fun decode(): String
    fun valid() : Boolean

    companion object {
        fun analyze(interval: String): Result<SlotSelector> {
            try {
                if (!interval.contains("..")) {
                    return Result.success(Single(Integer.parseInt(interval)))
                }

                val str = interval.split("..")
                Asserts.check(str.size == 2, "")
                val leftClose = str[0].endsWith("=")
                val rightClose = str[1].startsWith("=")
                val begin = Integer.parseInt(if (leftClose) str[0].dropLast(1) else str[0])
                val end = Integer.parseInt(if (rightClose) str[1].drop(1) else str[1])

                val selector = if (leftClose) {
                    if (rightClose) CloseInterval(begin, end) else CloseOpenInterval(begin, end)
                } else {
                    if (rightClose) OpenCloseInterval(begin, end) else OpenInterval(begin, end)
                }
                return Result.success(selector)
            } catch (e: Exception) {
                return Result.failure(RuntimeException("error while analyze $interval"))
            }
        }
    }

    data class Single(val slot: Int) : SlotSelector {
        override fun test(value: Int): Boolean = value == slot
        override fun decode(): String = "$slot"
        override fun valid(): Boolean = slot >= 0
    }

    data class OpenInterval(val left: Int, val right: Int) : SlotSelector {
        override fun test(value: Int): Boolean = value > left && value < right
        override fun decode(): String = "($left..$right)"
        override fun valid(): Boolean = right - 3 > left && left >= 0
    }

    data class CloseInterval(val left: Int, val right: Int) : SlotSelector {
        override fun test(value: Int): Boolean = value >= left && value <= right
        override fun decode(): String = "($left=..=$right)"
        override fun valid(): Boolean = right > left && left >= 0
    }

    data class CloseOpenInterval(val left: Int, val right: Int) : SlotSelector {
        override fun test(value: Int): Boolean = value >= left && value < right
        override fun decode(): String = "($left=..$right)"
        override fun valid(): Boolean = right -2 > left && left > 0
    }

    data class OpenCloseInterval(val left: Int, val right: Int) : SlotSelector {
        override fun test(value: Int): Boolean = value > left && value <= right
        override fun decode(): String = "($left..=$right)"
        override fun valid(): Boolean = right -2 > left && left > 0
    }

}