package com.github.zomb_676.cargo_hologram.compact.jei

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.ui.CraftMenu
import com.github.zomb_676.cargo_hologram.ui.CraftScreen
import com.github.zomb_676.cargo_hologram.ui.MonitorScreen
import com.github.zomb_676.cargo_hologram.util.cursor.AreaImmute
import com.github.zomb_676.cargo_hologram.util.log
import com.github.zomb_676.cargo_hologram.util.optional
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.RecipeTypes
import mezz.jei.api.gui.handlers.IGhostIngredientHandler
import mezz.jei.api.gui.handlers.IGuiContainerHandler
import mezz.jei.api.gui.handlers.IGuiProperties
import mezz.jei.api.gui.ingredient.IRecipeSlotView
import mezz.jei.api.gui.ingredient.IRecipeSlotsView
import mezz.jei.api.ingredients.ITypedIngredient
import mezz.jei.api.recipe.RecipeIngredientRole
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.recipe.transfer.IRecipeTransferError
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler
import mezz.jei.api.registration.IGuiHandlerRegistration
import mezz.jei.api.registration.IRecipeTransferRegistration
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.Rect2i
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingRecipe
import java.util.*

@JeiPlugin
@Suppress("unused")
class CargoHologramJeiPlugin : IModPlugin {
    override fun getPluginUid(): ResourceLocation = CargoHologram.rl("jei_plugin")

    override fun registerRecipeTransferHandlers(registration: IRecipeTransferRegistration) {
        registration.addUniversalRecipeTransferHandler(object : IRecipeTransferHandler<CraftMenu, CraftingRecipe> {
            override fun getContainerClass(): Class<out CraftMenu> = CraftMenu::class.java
            override fun getMenuType(): Optional<MenuType<CraftMenu>> = AllRegisters.CRAFTER_MANU.get().optional()
            override fun getRecipeType(): RecipeType<CraftingRecipe> = RecipeTypes.CRAFTING

            override fun transferRecipe(
                container: CraftMenu,
                recipe: CraftingRecipe,
                recipeSlots: IRecipeSlotsView,
                player: Player,
                maxTransfer: Boolean,
                doTransfer: Boolean,
            ): IRecipeTransferError? {
                if (doTransfer) {
                    val output: IRecipeSlotView = recipeSlots.getSlotViews(RecipeIngredientRole.OUTPUT).first()
                    val inputs: MutableList<IRecipeSlotView> = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT)

                }
                return null
            }

        })
    }

    override fun registerGuiHandlers(registration: IGuiHandlerRegistration) {
        registration.addGuiScreenHandler(MonitorScreen::class.java) { screen ->
            screen.mainArea.asIGuiProperties(screen)
        }
        registration.addGuiScreenHandler(CraftScreen::class.java) { screen ->
            screen.mainArea.asIGuiProperties(screen)
        }
        registration.addGhostIngredientHandler(
            CraftScreen::class.java,
            object : IGhostIngredientHandler<CraftScreen> {
                override fun <I : Any?> getTargetsTyped(
                    gui: CraftScreen,
                    ingredient: ITypedIngredient<I>,
                    doStart: Boolean,
                ): List<IGhostIngredientHandler.Target<I>> =
                    gui.craftMaterialSlotsArea().map { area ->
                        object : IGhostIngredientHandler.Target<I> {
                            override fun accept(ingredient: I) {
                                if (ingredient is ItemStack) {
                                    gui.set(area, ingredient)
                                }
                            }

                            override fun getArea(): Rect2i = area.asRect2i()
                        }
                    }


                override fun onComplete() {

                }

                override fun shouldHighlightTargets(): Boolean = true
            })
        registration.addGuiContainerHandler(CraftScreen::class.java, object : IGuiContainerHandler<CraftScreen> {


        })
    }

    /**
     * should return the area our screen used
     */
    private inline fun <reified T : Screen> AreaImmute.asIGuiProperties(screen: T): IGuiProperties =
        object : IGuiProperties {
            override fun getScreenClass(): Class<out Screen> = T::class.java
            override fun getGuiLeft(): Int = this@asIGuiProperties.x1
            override fun getGuiTop(): Int = this@asIGuiProperties.y1
            override fun getGuiXSize(): Int = this@asIGuiProperties.width
            override fun getGuiYSize(): Int = this@asIGuiProperties.height
            override fun getScreenWidth(): Int = screen.width
            override fun getScreenHeight(): Int = screen.height
        }
}