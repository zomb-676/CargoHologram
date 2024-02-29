package com.github.zomb_676.cargo_hologram.util.cursor


open class AreaMute(
    final override var left: Int,
    final override var up: Int,
    final override var right: Int,
    final override var down: Int,
) : AreaImmute(left, up, right, down) {
    final override var x1
        get() = left
        set(value) {
            left = value
        }
    final override var x2
        get() = right
        set(value) {
            right = value
        }
    final override var y1
        get() = up
        set(value) {
            up = value
        }
    final override var y2
        get() = down
        set(value) {
            down = value
        }
    final override var width
        get() = right - left
        set(value) {
            right = left + value
        }
    final override var height
        get() = down - up
        set(value) {
            down = up + value
        }

    override fun isolate(): AreaMute = AreaMute(left, up, right, down)
    override fun toString(): String = "AreaMute(left=$left, up=$up, right=$right, down=$down)"

    fun setArea(area: AreaImmute) {
        this.left = area.left
        this.up = area.up
        this.right = area.right
        this.down = area.down
    }

    fun setSize(area: AreaImmute) {
        this.width = area.width
        this.height = area.height
    }
}