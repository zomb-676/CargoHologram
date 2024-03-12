package com.github.zomb_676.cargo_hologram.util

import net.minecraft.util.FastColor
import net.minecraft.world.item.DyeColor

@JvmInline
@Suppress("unused")
value class ARGBColor private constructor(val color: Int) {
    companion object {
        private const val ALPHA_BIT_OFFSET = 8 * 3
        private const val RED_BIT_OFFSET = 8 * 2
        private const val GREEN_BIT_OFFSET = 8 * 1
        private const val BLUE_BIT_OFFSET = 8 * 0
        private const val ALPHA_MASK: Int = 0xff shl ALPHA_BIT_OFFSET
        private const val RED_MASK: Int = 0xff shl RED_BIT_OFFSET
        private const val GREEN_MASK: Int = 0xff shl GREEN_BIT_OFFSET
        private const val BLUE_MASK: Int = 0xff shl BLUE_BIT_OFFSET

        fun ofARGB(color: Int) = ARGBColor(color)
        fun of(red: Int, green: Int, blue: Int, alpha: Int = 0xff) =
            ARGBColor((alpha shl ALPHA_BIT_OFFSET) or (red shl RED_BIT_OFFSET) or (green shl GREEN_BIT_OFFSET) or (blue shl BLUE_BIT_OFFSET))
    }

    object Vanilla {
        val WHITE = ARGBColor(DyeColor.WHITE.textColor).fullAlpha()
        val ORANGE = ARGBColor(DyeColor.ORANGE.textColor).fullAlpha()
        val MAGENTA = ARGBColor(DyeColor.MAGENTA.textColor).fullAlpha()
        val LIGHT_BLUE = ARGBColor(DyeColor.LIGHT_BLUE.textColor).fullAlpha()
        val YELLOW = ARGBColor(DyeColor.YELLOW.textColor).fullAlpha()
        val LIME = ARGBColor(DyeColor.LIME.textColor).fullAlpha()
        val PINK = ARGBColor(DyeColor.PINK.textColor).fullAlpha()
        val GRAY = ARGBColor(DyeColor.GRAY.textColor).fullAlpha()
        val LIGHT_GRAY = ARGBColor(DyeColor.LIGHT_GRAY.textColor).fullAlpha()
        val CYAN = ARGBColor(DyeColor.CYAN.textColor).fullAlpha()
        val PURPLE = ARGBColor(DyeColor.PURPLE.textColor).fullAlpha()
        val BLUE = ARGBColor(DyeColor.BLUE.textColor).fullAlpha()
        val BROWN = ARGBColor(DyeColor.BROWN.textColor).fullAlpha()
        val GREEN = ARGBColor(DyeColor.GREEN.textColor).fullAlpha()
        val RED = ARGBColor(DyeColor.RED.textColor).fullAlpha()
        val BLACK = ARGBColor(DyeColor.BLACK.textColor).fullAlpha()
    }

    object Presets {
        val GREY = of(0x7f, 0x7f, 0x7f).fullAlpha()
        val WHITE = of(0xff, 0xff, 0xff).fullAlpha()
    }

    fun alpha(): Int = color ushr ALPHA_BIT_OFFSET and 0xff
    fun red(): Int = color ushr RED_BIT_OFFSET and 0xff
    fun green(): Int = color ushr GREEN_BIT_OFFSET and 0xff
    fun blue(): Int = color ushr BLUE_BIT_OFFSET and 0xff

    fun alpha(value: Int): ARGBColor = ARGBColor((color and ALPHA_MASK.inv()) or (value shl ALPHA_BIT_OFFSET))
    fun red(value: Int): ARGBColor = ARGBColor((color and RED_MASK.inv()) or (value shl RED_BIT_OFFSET))
    fun green(value: Int): ARGBColor = ARGBColor((color and GREEN_MASK.inv()) or (value shl GREEN_BIT_OFFSET))
    fun blue(value: Int): ARGBColor = ARGBColor((color and BLUE_MASK.inv()) or (value shl BLUE_BIT_OFFSET))

    fun halfAlpha() = alpha(0x7f)
    fun fullAlpha() = alpha(0xff)

    override fun toString(): String = "(a:${alpha()},r:${red()},g:${green()},b:${blue()})"
}