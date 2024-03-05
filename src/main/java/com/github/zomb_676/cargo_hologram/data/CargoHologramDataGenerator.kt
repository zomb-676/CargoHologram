package com.github.zomb_676.cargo_hologram.data

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.client.model.generators.ModelFile
import net.minecraftforge.common.data.LanguageProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject


object CargoHologramDataGenerator : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<GatherDataEvent> { event ->
            event.generator.addProvider(event.includeClient(), ModelProvider(event))
            event.generator.addProvider(event.includeClient(), EnglishLangProvider(event))
        }
    }

    class ModelProvider(event: GatherDataEvent) :
        ItemModelProvider(event.generator.packOutput, CargoHologram.MOD_ID, event.existingFileHelper) {
        companion object {
            val GENERATED: ResourceLocation = ResourceLocation("item/generated")
            val HANDHELD: ResourceLocation = ResourceLocation("item/handheld")
        }

        override fun registerModels() {
            AllRegisters.Items.apply {
                monitor.useItemModel(Items.DIAMOND)
                crafter.useItemModel(Items.DIAMOND)
                filter.useItemModel(Items.DIAMOND)
                glasses.useItemModel(Items.DIAMOND)
            }
        }

        private fun RegistryObject<out Item>.useItemModel(item: Item) {
            val location = ForgeRegistries.ITEMS.getKey(this.get())!!
            val useItemLocation = ForgeRegistries.ITEMS.getKey(item)!!
            val parentModelLocation = useItemLocation.withPath("item/${useItemLocation.path}")
            val parentModelFile = ModelFile.ExistingModelFile(parentModelLocation, existingFileHelper)
            getBuilder(location.toString())
                .parent(parentModelFile)
        }

        private fun RegistryObject<out Item>.singleTextureItem(resourceLocation: ResourceLocation) {
            val location = ForgeRegistries.ITEMS.getKey(this.get())!!

            getBuilder(location.toString())
                .parent(ModelFile.UncheckedModelFile(GENERATED))
                .texture("layer0", resourceLocation)
        }

        private fun RegistryObject<out Item>.singleTextureItem(textureName: String) {
            singleTextureItem(modLoc(textureName))
        }

        private fun RegistryObject<out Item>.singleTextureItem() {
            basicItem(this.get())
        }
    }

    class EnglishLangProvider(event: GatherDataEvent) :
        LanguageProvider(event.generator.packOutput, CargoHologram.MOD_ID, "en_us") {
        override fun addTranslations() {
            AllRegisters.Items.apply {
                monitor.lang("Monitor")
                crafter.lang("Crafter")
                filter.lang("Filter")
                glasses.lang("Monitor Glasses")
            }
        }

        private fun RegistryObject<out Item>.lang(trans: String) {
            addItem(this, trans)
        }

    }
}