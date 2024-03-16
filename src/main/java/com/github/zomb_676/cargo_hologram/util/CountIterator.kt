package com.github.zomb_676.cargo_hologram.util

class CountIterator<T>(val iterator: Iterator<T>) : Iterator<T> {

    private var count = 0

    override fun hasNext(): Boolean = iterator.hasNext()
    override fun next(): T {
        count++
        return iterator.next()
    }

    fun getCount(): Int = count

}