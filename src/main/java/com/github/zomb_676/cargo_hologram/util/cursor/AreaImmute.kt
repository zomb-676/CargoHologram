package com.github.zomb_676.cargo_hologram.util.cursor

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.navigation.ScreenRectangle
import net.minecraft.client.renderer.Rect2i

open class AreaImmute(
    override val left: Int,
    override val up: Int,
    override val right: Int,
    override val down: Int,
) : IArea {

    companion object {
        fun ofFullScreen() = run {
            val window = Minecraft.getInstance().window
            AreaImmute(0, 0, window.guiScaledWidth, window.guiScaledHeight)
        }

        fun ofSize(width: Int, height: Int) = AreaImmute(0, 0, width, height)

        fun ofRelative(x: Int, y: Int, width: Int, height: Int) = AreaImmute(x, y, x + width, y + height)
    }

    constructor(area: AreaImmute) : this(area.left, area.up, area.right, area.down)

    open fun isolate(): AreaImmute = AreaImmute(left, up, right, down)

    override fun toString(): String = "AreaImmute(left=$left, up=$up, right=$right, down=$down)"
    fun toStringShort() = "Area($left,$up,$right,$down)"

    fun asAreaImmute() = AreaImmute(left, up, right, down)
    fun asAreaMute() = AreaMute(left, up, right, down)
    fun asBaseCursor(): Cursor<*> = Cursor(left, up, right, down)

    fun assertValidate() {
        if (this.up >= this.down || this.left >= this.right) throw IllegalStateException("${toStringShort()} is invalid")
    }

    fun asScreenRectangle() = ScreenRectangle(x1, y1, width, height)
    fun asRect2i() = Rect2i(x1, y1, width, height)
}
