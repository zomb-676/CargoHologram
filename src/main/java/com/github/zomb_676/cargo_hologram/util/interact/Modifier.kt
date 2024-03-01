package com.github.zomb_676.cargo_hologram.util.interact

import org.lwjgl.glfw.GLFW

@JvmInline
value class Modifier(val modifier: Int) {
    inline val isShiftDown get() = modifier and GLFW.GLFW_MOD_SHIFT != 0
    inline val isControlDown get() = modifier and GLFW.GLFW_MOD_CONTROL != 0
    inline val isAltDown get() = modifier and GLFW.GLFW_MOD_ALT != 0
    inline val isSuperDown get() = modifier and GLFW.GLFW_MOD_SUPER != 0
    inline val isCapsLockDown get() = modifier and GLFW.GLFW_MOD_CAPS_LOCK != 0
    inline val isNumLockDown get() = modifier and GLFW.GLFW_MOD_NUM_LOCK != 0

    override fun toString(): String {
        val builder = StringBuilder()
        if (isShiftDown) builder.append("Shift,")
        if (isControlDown) builder.append("Control,")
        if (isAltDown) builder.append("Alt,")
        if (isSuperDown) builder.append("Super,")
        if (isCapsLockDown) builder.append("CapsLock,")
        if (isNumLockDown) builder.append("NumLock,")
        return if (builder.isEmpty()) {
            "Modifier(None)"
        } else {
            "Modifier(${builder.dropLast(1)})"
        }
    }
}