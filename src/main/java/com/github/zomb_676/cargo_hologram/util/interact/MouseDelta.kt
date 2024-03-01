package com.github.zomb_676.cargo_hologram.util.interact

import kotlin.math.sign

@JvmInline
value class MouseDelta(val delta: Double) {
    inline val isPositive get() = delta > 0
    inline val isNegative get() = delta < 0
    inline val sign get() = delta.sign

    override fun toString(): String = "MouseDelta($delta)"
}