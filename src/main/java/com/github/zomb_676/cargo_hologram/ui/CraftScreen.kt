package com.github.zomb_676.cargo_hologram.ui

import com.github.zomb_676.cargo_hologram.util.literal
import net.minecraft.client.gui.screens.inventory.ContainerScreen
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.ChestMenu

class CraftScreen(pPlayerInventory: Inventory, pMenu: ChestMenu) :
    ContainerScreen(pMenu, pPlayerInventory, "craft".literal())