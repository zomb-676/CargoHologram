package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.Config
import com.github.zomb_676.cargo_hologram.capability.CapRegisters
import com.github.zomb_676.cargo_hologram.capability.CargoEnergyItemCapability
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.util.OpenBy
import com.github.zomb_676.cargo_hologram.util.literal
import com.github.zomb_676.cargo_hologram.util.retrive
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.network.NetworkHooks
import kotlin.jvm.optionals.getOrNull

class CraftPanel : Item(Properties().stacksTo(1)) {
    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!pLevel.isClientSide) {
            val heldItem = pPlayer.getItemInHand(pUsedHand)
            val capability = heldItem.getCapability(CapRegisters.CARGO_ENERGY_ITEM).retrive()
                ?: return InteractionResultHolder.fail(heldItem)
            if (capability.current < Config.Server.perOpenConsume) {
                pPlayer.sendSystemMessage("need at least ${Config.Server.perOpenConsume} FE to open".literal())
                return InteractionResultHolder.fail(heldItem)
            }
            capability.current-= Config.Server.perOpenConsume
            val openBy = OpenBy.byItem(heldItem)
            NetworkHooks.openScreen(pPlayer as ServerPlayer, object : MenuProvider {
                override fun createMenu(
                    pContainerId: Int,
                    pPlayerInventory: Inventory,
                    pPlayer: Player,
                ): AbstractContainerMenu = CraftMenu(pContainerId, pPlayerInventory, openBy)

                override fun getDisplayName(): Component = "".literal()
            }, openBy::write)
        }
        return super.use(pLevel, pPlayer, pUsedHand)
    }

    override fun appendHoverText(stack: ItemStack, level: Level?, tips: MutableList<Component>, advance: TooltipFlag) {
        try {
            stack.getCapability(CapRegisters.CARGO_ENERGY_ITEM).ifPresent { cap ->
                tips += "energy:${cap.current}/${cap.max}".literal()
            }
        } catch (e: Throwable) {
            println()
        }
    }

    override fun isBarVisible(pStack: ItemStack): Boolean = true

    override fun getBarWidth(pStack: ItemStack): Int {
        val cap = pStack.getCapability(CapRegisters.CARGO_ENERGY_ITEM).resolve().getOrNull() ?: return 0
        return (cap.energyRemainPercent() * 13).toInt()
    }

    override fun getBarColor(pStack: ItemStack): Int {
        val cap = pStack.getCapability(CapRegisters.CARGO_ENERGY_ITEM).resolve().getOrNull() ?: return 0
        return net.minecraft.util.Mth.hsvToRgb((cap.energyRemainPercent() / 3.0).toFloat(), 1f, 1f)
    }

    override fun initCapabilities(stack: ItemStack, nbt: CompoundTag?): ICapabilityProvider? {
        if (nbt == null) return null
        return CapRegisters.CargoEnergyItemProvider(CargoEnergyItemCapability.of(nbt))
    }
}