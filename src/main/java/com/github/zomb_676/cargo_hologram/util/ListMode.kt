package com.github.zomb_676.cargo_hologram.util

enum class ListMode(val fallback : Boolean) {
    BLACK_LIST_MODE(true),
    WHITE_LIST_MODE(false),
    IGNORE(true)
}