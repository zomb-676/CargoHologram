package com.github.zomb_676.cargo_hologram.item

import com.github.zomb_676.cargo_hologram.util.literal
import com.github.zomb_676.cargo_hologram.util.location
import com.github.zomb_676.cargo_hologram.util.plus
import com.github.zomb_676.cargo_hologram.util.toBlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.registries.ForgeRegistries

class LinkerItem : Item(Properties().stacksTo(1)) {
    object LinkData {
        private const val COMPOUND_TAG_NAME = "linker_data"
        private const val COMPUND_LINK_STATE_NAME = "link_state"
        private const val COMPOUND_LIST_TAG_SELECTED_NAME = "selected"
        private const val COMPOUND_SELECTED_TYPE_KEY = "type"
        private const val COMPOUND_SELECTED_POS_KEY = "pos"

        fun haveTag(itemStack: ItemStack) =
            itemStack.tag?.contains(COMPOUND_TAG_NAME, Tag.TAG_COMPOUND.toInt()) ?: false

        fun isLinking(itemStack: ItemStack): Boolean {
            val tag = itemStack.tag?.getCompound(COMPOUND_TAG_NAME) ?: return false
            return tag.getBoolean(COMPUND_LINK_STATE_NAME)
        }

        fun setLinking(itemStack: ItemStack, state: Boolean) {
            val tag = itemStack.tag!!.getCompound(COMPOUND_TAG_NAME)
            tag.putBoolean(COMPUND_LINK_STATE_NAME, state)
            if (state) {
                tag.getList(COMPOUND_LIST_TAG_SELECTED_NAME, Tag.TAG_COMPOUND.toInt()).clear()
            }
        }

        fun getLinkCount(itemStack: ItemStack): Int =
            itemStack.tag!!.getCompound(COMPOUND_TAG_NAME)
                .getList(COMPOUND_LIST_TAG_SELECTED_NAME, Tag.TAG_COMPOUND.toInt()).size

        fun init(itemStack: ItemStack) {
            val tag = CompoundTag()
            tag.putBoolean(COMPUND_LINK_STATE_NAME, false)
            tag.put(COMPOUND_LIST_TAG_SELECTED_NAME, ListTag())
            itemStack.orCreateTag.put(COMPOUND_TAG_NAME, tag)
        }

        fun addLinked(itemStack: ItemStack, blockEntity: BlockEntity): Boolean {
            val tag = itemStack.tag!!.getCompound(COMPOUND_TAG_NAME)
                .getList(COMPOUND_LIST_TAG_SELECTED_NAME, Tag.TAG_COMPOUND.toInt())
            val index = tag.indexOfFirst {
                (it as CompoundTag).getLong(COMPOUND_SELECTED_POS_KEY).toBlockPos() == blockEntity.blockPos
            }
            if (index == -1) {
                val selectedTag = CompoundTag()
                selectedTag.putLong(COMPOUND_SELECTED_POS_KEY, blockEntity.blockPos.asLong())
                selectedTag.putString(
                    COMPOUND_SELECTED_TYPE_KEY,
                    blockEntity.type.location(ForgeRegistries.BLOCK_ENTITY_TYPES).toString()
                )
                return true
            } else {
                val selected = tag[index] as CompoundTag
                selected.putLong(COMPOUND_SELECTED_POS_KEY, blockEntity.blockPos.asLong())
                selected.putString(
                    COMPOUND_SELECTED_TYPE_KEY,
                    blockEntity.type.location(ForgeRegistries.BLOCK_ENTITY_TYPES).toString()
                )
                return false
            }
        }
    }

    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        val itemStack = pPlayer.getItemInHand(pUsedHand)
        if (!LinkData.haveTag(itemStack)) LinkData.init(itemStack)
        val linking = LinkData.isLinking(itemStack)
        if (pPlayer.isShiftKeyDown) {
            LinkData.setLinking(itemStack, !linking)
            if (!pLevel.isClientSide) {
                val message = if (linking) {
                    "begin linking"
                } else {
                    "end linking, linked ${LinkData.getLinkCount(itemStack)}"
                }
                pPlayer.sendSystemMessage(message.literal())
            }
            return InteractionResultHolder.success(itemStack)
        }
        return super.use(pLevel, pPlayer, pUsedHand)
    }

    override fun useOn(pContext: UseOnContext): InteractionResult {
        val level = pContext.level
        val pos = pContext.clickedPos
        val blockEntity = level.getBlockEntity(pos)
        val player = pContext.player ?: return InteractionResult.FAIL
        if (blockEntity == null) {
            val blockState = level.getBlockState(pos)
            player.sendSystemMessage("clicked at ".literal() + blockState.block.name + " hover, it not have a BlockEntity".literal())
            return InteractionResult.FAIL
        }
        val capability = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER)
        if (!capability.isPresent) {
            player.sendSystemMessage(
                "clicked at ".literal() + blockEntity.type.location(ForgeRegistries.BLOCK_ENTITY_TYPES).toString()
                    .literal() + " but it not support store items by automatic way".literal()
            )
            return InteractionResult.FAIL
        }
        val res = LinkData.addLinked(pContext.itemInHand, blockEntity)
        if (res) {
            player.sendSystemMessage("link ${blockEntity.blockPos.toShortString()} success ".literal())
        } else {
            player.sendSystemMessage("replace already added ${blockEntity.blockPos.toShortString()}".literal())
        }
        return InteractionResult.SUCCESS
    }

    override fun isFoil(pStack: ItemStack): Boolean = LinkData.isLinking(pStack)
}