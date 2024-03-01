package com.github.zomb_676.cargo_hologram.util.interact

import org.lwjgl.glfw.GLFW

@JvmInline
value class MouseButton(val button: Int) {
    inline val isLeft get() = button == GLFW.GLFW_MOUSE_BUTTON_LEFT
    inline val isRight get() = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
    inline val isMiddle get() = button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE

    override fun toString(): String {
        val name = when (button) {
            GLFW.GLFW_MOUSE_BUTTON_LEFT -> "left"
            GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "right"
            GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "middle"
            else -> button.toString()
        }
        return "Button($name)"
    }
}