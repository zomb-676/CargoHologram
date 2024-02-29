package com.github.zomb_676.cargo_hologram.util.cursor

interface AreaTransform<T> {
    fun remainWidth(needWidth: Int): Boolean
    fun remainHeight(needHeight: Int): Boolean
    fun assertRemainWidth(needWidth: Int): T
    fun assertRemainHeight(needHeight: Int): T

    fun upDown(offset: Int): T
    fun upUp(offset: Int): T
    fun downUp(offset: Int): T
    fun downDown(offset: Int): T
    fun leftLeft(offset: Int): T
    fun leftRight(offset: Int): T
    fun rightRight(offset: Int): T
    fun rightLeft(offset: Int): T

    fun assignUp(offset: Int, assigned: T.() -> Unit): T
    fun assignDown(offset: Int, assigned: T.() -> Unit): T
    fun assignLeft(offset: Int, assigned: T.() -> Unit): T
    fun assignRight(offset: Int, assigned: T.() -> Unit): T

    fun assignUp(offset: Int): T
    fun assignDown(offset: Int): T
    fun assignLeft(offset: Int): T
    fun assignRight(offset: Int): T

    fun innerX(offset: Int): T
    fun innerY(offset: Int): T
    fun inner(offset: Int): T
    fun percentX(offset: Double): T
    fun percentY(offset: Double): T
    fun percent(offset: Double): T
    fun expandX(offset: Int): T
    fun expandY(offset: Int): T
    fun expand(offset: Int): T
    fun moveDown(offset: Int): T
    fun moveUp(offset: Int): T
    fun moveLeft(offset: Int): T
    fun moveRight(offset: Int): T

    fun isolate(): T
    fun self(): T
}