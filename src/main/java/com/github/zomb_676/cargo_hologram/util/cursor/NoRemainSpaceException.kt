package com.github.zomb_676.cargo_hologram.util.cursor

class NoRemainSpaceException(reason: String) : Exception(reason) {
    companion object {
        @Throws(NoRemainSpaceException::class)
        fun noWidthSpace(needWidth: Int, remainWith: Int): Nothing =
            throw NoRemainSpaceException("need width:$needWidth, remain:$remainWith")

        @Throws(NoRemainSpaceException::class)
        fun noHeightSpace(needHeight: Int, remainHeight: Int): Nothing =
            throw NoRemainSpaceException("need height:$needHeight, remain:$remainHeight")

        fun <T : Cursor<T>> Cursor<T>.noWidth(needWidth: Int): Nothing = noWidthSpace(needWidth, this.width)
        fun <T : Cursor<T>> Cursor<T>.noHeight(needHeight: Int): Nothing = noHeightSpace(needHeight, this.height)
    }
}
