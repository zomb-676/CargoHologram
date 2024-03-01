package com.github.zomb_676.cargo_hologram.util.interact

import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

@Suppress("unused", "MemberVisibilityCanBePrivate")
object InteractHelper {
    fun ofButton(button: Int) = MouseButton(button)
    fun ofModifier(modifier: Int) = Modifier(modifier)
    fun ofDelta(delta: Double) = MouseDelta(delta)


    private fun currentWindow() = Minecraft.getInstance().window.window

    fun currentMouse(window: Long = currentWindow()): MouseButton {
        val pressed = (0..GLFW.GLFW_MOUSE_BUTTON_LAST).first {
            GLFW.glfwGetMouseButton(window, it) == GLFW.GLFW_PRESS
        }
        return ofButton(pressed)
    }

    fun keyPressed(key: Int, window: Long = currentWindow()) =
        GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS

    fun currentModifiers(window: Long = currentWindow()): Modifier {
        var modifier = 0
        if (keyPressed(GLFW.GLFW_KEY_LEFT_SHIFT, window) or keyPressed(GLFW.GLFW_KEY_RIGHT_SHIFT, window))
            modifier = modifier or GLFW.GLFW_MOD_SHIFT
        if (keyPressed(GLFW.GLFW_KEY_LEFT_CONTROL, window) or keyPressed(GLFW.GLFW_KEY_RIGHT_CONTROL, window))
            modifier = modifier or GLFW.GLFW_MOD_CONTROL
        if (keyPressed(GLFW.GLFW_KEY_LEFT_ALT, window) or keyPressed(GLFW.GLFW_KEY_RIGHT_ALT, window))
            modifier = modifier or GLFW.GLFW_MOD_ALT
        if (keyPressed(GLFW.GLFW_KEY_LEFT_SUPER, window) or keyPressed(GLFW.GLFW_KEY_RIGHT_SUPER, window))
            modifier = modifier or GLFW.GLFW_MOD_SUPER
        //TODO
//        if (GLFW.glfwGetInputMode(window, GLFW.GLFW_LOCK_KEY_MODS) == GLFW.GLFW_TRUE)
//            modifier = modifier or GLFW.GLFW_MOD_CAPS_LOCK
//        if (GLFW.glfwGetInputMode(window, GLFW.GLFW_LOCK_KEY_MODS) == GLFW.GLFW_TRUE)
//            modifier = modifier or GLFW.GLFW_MOD_NUM_LOCK
        return ofModifier(modifier)
    }


}