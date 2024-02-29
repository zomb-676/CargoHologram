package com.github.zomb_676.cargo_hologram.util

import kotlin.math.max

class MoveData(x: Int, y: Int) {
    var anchorX: Int = 0
        private set
    var anchorY: Int = 0
        private set

    var x: Int = x
        private set
    var y: Int = y
        private set
    var spanY: Int = 0
        private set

    fun appendY(y : Int) : MoveData {
        this.y += y
        return this
    }

    fun appendX(x: Int): MoveData {
        this.x += x
        return this
    }

    fun spanY(y: Int): MoveData {
        spanY = max(spanY, y)
        return this
    }

    fun move(): MoveData {
        this.x = anchorX
        this.y += spanY
        return this
    }

    fun newAnchor(): MoveData {
        this.anchorX = this.x
        this.anchorY = this.y
        this.spanY = 0
        return this
    }

    fun toAnchor(): MoveData {
        this.x = anchorX
        this.y = anchorY
        this.spanY = 0
        return this
    }

    fun clearAnchor(): MoveData {
        this.anchorX = 0
        this.anchorY = 0
        this.toAnchor()
        return this
    }
}