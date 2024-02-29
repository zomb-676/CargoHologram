package com.github.zomb_676.cargo_hologram.util.cursor

import com.github.zomb_676.cargo_hologram.util.cursor.NoRemainSpaceException.Companion.noHeight
import com.github.zomb_676.cargo_hologram.util.cursor.NoRemainSpaceException.Companion.noWidth
import net.minecraft.client.gui.GuiGraphics
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class Cursor<T : Cursor<T>>(
    left: Int,
    up: Int,
    right: Int,
    down: Int,
    private val layers: Deque<AreaMute> = ArrayDeque(0),
) : AreaMute(left, up, right, down), AreaTransform<T> {

    @PublishedApi
    internal val layerSize get() = layers.size

    @Suppress("UNCHECKED_CAST")
    override fun self(): T = this as T

    @OptIn(ExperimentalContracts::class)
    inline fun self(codeBlock: (T).() -> Unit): T {
        contract { callsInPlace(codeBlock, InvocationKind.EXACTLY_ONCE) }
        val self = self()
        codeBlock(self)
        return self
    }

    @OptIn(ExperimentalContracts::class)
    inline fun use(codeBlock: T.() -> Unit) {
        contract { callsInPlace(codeBlock, InvocationKind.EXACTLY_ONCE) }
        this.pushLayer()
        val beforeLayerSize = layerSize
        codeBlock(self())
        if (beforeLayerSize != layerSize) {
            throw IllegalStateException("layer size should be the same")
        }
        this.popLayer()
    }

    fun pushLayer(): T {
        layers.addLast(this.isolate())
        return self()
    }

    fun popLayer(): T {
        val last = layers.pollLast() ?: throw IllegalStateException("layer under flow")
        this.left = last.left
        this.up = last.up
        this.right = last.right
        this.down = last.down
        return self()
    }

    fun forDraw(guiGraphics: GuiGraphics) = GraphicCursor(this, guiGraphics)

    @Suppress("UNCHECKED_CAST")
    override fun isolate(): T {
        return asBaseCursor() as T
    }

    override fun remainWidth(needWidth: Int) = needWidth < width
    override fun remainHeight(needHeight: Int) = needHeight < height
    override fun assertRemainWidth(needWidth: Int) = self { if (!remainWidth(needWidth)) noWidth(needWidth) }
    override fun assertRemainHeight(needHeight: Int) = self { if (!remainHeight(needHeight)) noHeight(needHeight) }

    override fun upDown(offset: Int) = self { up += offset }
    override fun upUp(offset: Int) = self { up -= offset }
    override fun downUp(offset: Int) = self { down -= offset }
    override fun downDown(offset: Int) = self { down += offset }
    override fun leftLeft(offset: Int) = self { left -= offset }
    override fun leftRight(offset: Int) = self { left += offset }
    override fun rightRight(offset: Int) = self { right += offset }
    override fun rightLeft(offset: Int) = self { right -= offset }

    override fun assignUp(offset: Int, assigned: T.() -> Unit) = self {
        assertRemainHeight(offset)
        use {
            this.downUp(height - offset)
            assigned(this)
        }
        this.upDown(offset)
    }

    override fun assignDown(offset: Int, assigned: T.() -> Unit) = self {
        assertRemainHeight(offset)
        use {
            this.upDown(height - offset)
            assigned(this)
        }
        this.downUp(offset)
    }

    override fun assignLeft(offset: Int, assigned: T.() -> Unit) = self {
        assertRemainWidth(offset)
        use {
            this.rightLeft(width - offset)
            assigned(this)
        }
        this.leftRight(offset)
    }

    override fun assignRight(offset: Int, assigned: T.() -> Unit) = self {
        assertRemainWidth(offset)
        use {
            this.leftRight(width - offset)
            assigned(this)
        }
        this.rightLeft(offset)
    }

    override fun assignUp(offset: Int): T {
        assertRemainHeight(offset)
        val copy = this.isolate()
        copy.downUp(height - offset)
        this.upDown(offset)
        return copy
    }

    override fun assignDown(offset: Int): T {
        assertRemainHeight(offset)
        val copy = this.isolate()
        copy.upDown(height - offset)
        this.downUp(offset)
        return copy
    }

    override fun assignLeft(offset: Int): T {
        assertRemainWidth(offset)
        val copy = this.isolate()
        copy.rightLeft(width - offset)
        this.leftRight(offset)
        return copy
    }

    override fun assignRight(offset: Int): T {
        assertRemainWidth(offset)
        val copy = this.isolate()
        copy.leftRight(width - offset)
        this.rightLeft(offset)
        return copy
    }

    override fun innerX(offset: Int): T = self { assertRemainWidth(2 * offset).leftRight(offset).rightLeft(offset) }
    override fun innerY(offset: Int): T = self { assertRemainHeight(2 * offset).upDown(offset).downUp(offset) }
    override fun inner(offset: Int): T = self { innerX(offset).innerY(offset) }

    override fun percentX(offset: Double): T = self { innerX((width * (1.0 - offset) / 2).toInt()) }
    override fun percentY(offset: Double): T = self { innerY((height * (1.0 - offset) / 2).toInt()) }
    override fun percent(offset: Double): T = self { percentX(offset).percentY(offset) }

    override fun expandX(offset: Int): T = self { leftLeft(offset).rightRight(offset) }
    override fun expandY(offset: Int): T = self { upUp(offset).downDown(offset) }
    override fun expand(offset: Int): T = self { expandX(offset).expandY(offset) }

    override fun moveDown(offset: Int): T = self { upDown(offset).downDown(offset) }
    override fun moveUp(offset: Int): T = self { upUp(offset).downUp(offset) }
    override fun moveLeft(offset: Int): T = self { leftLeft(offset).rightLeft(offset) }
    override fun moveRight(offset: Int): T = self { leftRight(offset).rightRight(offset) }

    override fun toString(): String = "Cursor(left=$left, up=$up, right=$right, down=$down, layers:${layerSize})"

}
