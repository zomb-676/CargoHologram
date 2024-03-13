package com.github.zomb_676.cargo_hologram.util

import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.util.cursor.Cursor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.core.BlockPos
import net.minecraft.core.SectionPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.network.NetworkDirection
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.server.ServerLifecycleHooks
import org.apache.http.util.Asserts
import org.slf4j.Logger
import java.util.*

fun String.literal(): MutableComponent = Component.literal(this)
fun String.translate() = Component.translatable(this)
fun String.translate(vararg parameters: Any) = Component.translatable(this, *parameters)

fun currentServer() = ServerLifecycleHooks.getCurrentServer()!!
fun currentMinecraft() = Minecraft.getInstance()
fun currentClientPlayer() = currentMinecraft().player!!
fun currentRegistryAccess() = currentServer().registryAccess()
fun currentMcFont() = Minecraft.getInstance().font

fun ResourceLocation.dimKey(): ResourceKey<Level> = ResourceKey.create(Registries.DIMENSION, this)
fun ResourceLocation.dim(): ServerLevel = currentServer().getLevel(this.dimKey())!!
fun ResourceKey<Level>.dim() = currentServer().getLevel(this)!!

fun allLevel(): MutableIterable<ServerLevel> = currentServer().allLevels

fun isOnDev() = !FMLEnvironment.production
fun isOnProduct() = FMLEnvironment.production
inline fun onDev(codeBlock: () -> Unit) {
    if (isOnDev()) codeBlock()
}

inline fun onProuct(codeBlock: () -> Unit) {
    if (isOnProduct()) codeBlock()
}

inline fun onDebug(codeBlock: () -> Unit) {
    if (Config.Server.enableDebug) codeBlock()
}

/**
 * @param T see samples
 * @sample throwOnDevExample
 */
fun <T> throwOnDev(exception: Exception = RuntimeException("encounter potential problem, this only throws on dev")): T? {
    onDev { throw exception }
    log { error("error happen, not throw on production", exception) }
    return null
}

@Suppress("UNUSED_VARIABLE", "UNCHECKED_CAST")
@Deprecated(message = "for example only")
private fun <T, U> throwOnDevExample(obj: T) {
    val u: U = obj as U ?: throwOnDev() ?: return
}


fun debugAssert(require: Boolean, errorMessage: String) {
    if (require) return
    onDev { log { throw AssertionError(errorMessage) } }
}

inline fun debugAssert(require: Boolean, errorMessage: () -> String) {
    if (require) return
    onDev { log { throw AssertionError(errorMessage.invoke()) } }
}

inline fun log(codeBlock: Logger.() -> Unit) = codeBlock(CargoHologram.LOGGER)
inline fun logOnDebug(codeBlock: Logger.() -> Unit) = onDebug { log(codeBlock) }

fun ServerPlayer.isOnline(): Boolean = currentServer().playerList.getPlayer(this.uuid) != null

fun UUID.queryPlayer(): ServerPlayer? = currentServer().playerList.getPlayer(this)

@JvmName("optionalForNoNullReceiver")
fun <T : Any> T.optional(): Optional<T> = Optional.of(this)

@JvmName("optionalForNullableReceiver")
@Suppress("UNCHECKED_CAST")
fun <T> T.optional() = Optional.ofNullable(this) as Optional<T>

fun Screen.open() = Minecraft.getInstance().tell { Minecraft.getInstance().setScreen(this) }

@Suppress("FunctionName")
fun IMPOSSIBLE(): Nothing = throw RuntimeException("never should run here")

fun NetworkDirection.assertPlayToServer() =
    Asserts.check(this == NetworkDirection.PLAY_TO_SERVER, "actual:$this,expect:PLAY_TO_SERVER")

fun NetworkDirection.assertPlayToClient() =
    Asserts.check(this == NetworkDirection.PLAY_TO_CLIENT, "actual:$this,expect:PLAY_TO_CLIENT")

fun NetworkDirection.assertLoginToServer() =
    Asserts.check(this == NetworkDirection.LOGIN_TO_SERVER, "actual:$this,expect:LOGIN_TO_SERVER")

fun NetworkDirection.assertLoginToClient() =
    Asserts.check(this == NetworkDirection.LOGIN_TO_CLIENT, "actual:$this,expect:LOGIN_TO_CLIENT")

//FriendlyByteBuf

fun Int.neighbourRange(radius: Int) = (this - radius)..(this + radius)

/**
 * radius, when set 0, only self
 */
inline fun ChunkPos.near(radius: Int, function: (ChunkPos) -> Unit) {
    if (radius == 0) {
        function(this)
    } else {
        for (x in this.x.neighbourRange(radius)) {
            for (z in this.z.neighbourRange(radius)) {
                function(ChunkPos(x, z))
            }
        }
    }
}

inline fun inlineAssert(check: Boolean, codeBlock: () -> String) {
    if (!check) {
        throw AssertionError(codeBlock())
    }
}

inline fun <T> List<T>.forEachDiffIndex(function: (T, T) -> Unit) = when (val size = this.size) {
    0 -> throw RuntimeException("not support for empty List")
    1 -> throw RuntimeException("not support for List with one entry")
    2 -> function(this[0], this[1])
    else -> {
        for (i in 0..<(size - 1)) {
            for (j in (i + 1)..<size) {
                function(this[i], this[j])
            }
        }
    }
}

fun ItemLike.asItemStack(): ItemStack = ItemStack(this)
fun ItemStack.gatherTooltip(): MutableList<Component> = Screen.getTooltipFromItem(Minecraft.getInstance(), this)
fun MutableList<Component>.append(component: Component): MutableList<Component> {
    this.add(component)
    return this
}

fun Array<out BusSubscribe>.dispatch() = this.forEach { s -> s.registerEvent(Dispatcher) }

inline fun runOnDistClient(crossinline f: () -> () -> Unit) {
    DistExecutor.safeRunWhenOn(Dist.CLIENT) { DistExecutor.SafeRunnable { f()() } }
}

fun AbstractWidget.assign(cursor: Cursor<*>) {
    cursor.setWidget(this)
}

fun BlockPos.sectionX() = SectionPos.blockToSectionCoord(this.x)
fun BlockPos.sectionY() = SectionPos.blockToSectionCoord(this.y)
fun BlockPos.sectionZ() = SectionPos.blockToSectionCoord(this.z)
fun BlockPos.toChunkPos() = ChunkPos(this)

fun Long.toBlockPos() = BlockPos.of(this)

fun <T> ResourceLocation.query(registries: IForgeRegistry<T>) = registries.getValue(this)!!
fun <T> T.location(registries: IForgeRegistry<T>) = registries.getKey(this)!!

operator fun MutableComponent.plus(component: Component): MutableComponent = this.append(component)
operator fun String.plus(component: Component): MutableComponent = this.literal().append(component)

fun AbstractWidget.isIn(mouseX: Double, mouseY: Double) =
    mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height

fun GuiGraphics.fillRelative(x: Int, y: Int, width: Int, height: Int, color: ARGBColor) {
    this.fill(x, y, x + width, y + height, color.color)
}