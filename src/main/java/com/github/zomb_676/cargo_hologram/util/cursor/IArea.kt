package com.github.zomb_676.cargo_hologram.util.cursor

interface IArea {
    val left: Int
    val up: Int
    val right: Int
    val down: Int

    val x1 get() = left
    val y1 get() = up
    val x2 get() = right
    val y2 get() = down
    val width get() = right - left
    val height get() = down - up

    fun xCenter() = (x1 + x2) / 2
    fun yCenter() = (y1 + y2) / 2

    infix fun isSameArea(checkArea: AreaImmute) =
        this.left == checkArea.left && this.right == checkArea.right && this.up == checkArea.up && this.down == checkArea.down

    fun containsPointIncludingEdge(x: Int, y: Int) = this.left <= x && this.right >= x && this.up <= y && this.down >= y

    fun containsPointExcludingEdge(x: Int, y: Int) = this.left < x && this.right > x && this.up < y && this.down > y

    fun checkIntersectExcludingEdge(checkArea: AreaImmute) =
        this.up >= checkArea.down || this.down <= checkArea.up || this.left >= checkArea.right || this.right <= checkArea.left

    fun checkIntersectIncludingEdge(checkArea: AreaImmute) =
        this.up > checkArea.down || this.down < checkArea.up || this.left > checkArea.right || this.right < checkArea.left
}