package com.github.zomb_676.cargo_hologram.data

import com.github.zomb_676.cargo_hologram.AllRegisters
import com.github.zomb_676.cargo_hologram.AllTranslates
import com.github.zomb_676.cargo_hologram.CargoHologram
import com.github.zomb_676.cargo_hologram.util.BusSubscribe
import com.github.zomb_676.cargo_hologram.util.Dispatcher
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.client.model.generators.BlockModelProvider
import net.minecraftforge.client.model.generators.BlockStateProvider
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.client.model.generators.ModelFile
import net.minecraftforge.common.data.LanguageProvider
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.RegistryObject


object CargoHologramDataGenerator : BusSubscribe {
    override fun registerEvent(dispatcher: Dispatcher) {
        dispatcher<GatherDataEvent> { event ->
            event.generator.addProvider(event.includeClient(), CargoItemModelProvider(event))
            event.generator.addProvider(event.includeClient(), CargoBlockModelProvider(event))
            event.generator.addProvider(event.includeClient(), EnglishLangProvider(event))
            event.generator.addProvider(event.includeClient(), CargoBlockStateProvider(event))
        }
    }

    class CargoItemModelProvider(event: GatherDataEvent) :
        ItemModelProvider(event.generator.packOutput, CargoHologram.MOD_ID, event.existingFileHelper) {
        companion object {
            val GENERATED: ResourceLocation = ResourceLocation("item/generated")
            val HANDHELD: ResourceLocation = ResourceLocation("item/handheld")
        }

        override fun registerModels() {
            AllRegisters.Items.apply {
                monitor.useItemModel(Items.DIAMOND)
                crafter.useItemModel(Items.DIAMOND)
                cargoFilter.useItemModel(Items.DIAMOND)
                panel.useItemModel(Items.DIAMOND)
                traitFilter.useItemModel(Items.NAME_TAG)
                listFilter.useItemModel(Items.NAME_TAG)
                configureUISTick.useItemModel(Items.DEBUG_STICK)
                remoteCraftTableItem.useItemModel(Items.CRAFTING_TABLE)
                cargoStorageItem.useItemModel(Items.BARREL)
                cargoInserter.useItemModel(Items.HOPPER)
                linker.useItemModel(Items.ECHO_SHARD)
            }
        }

        private fun RegistryObject<out Item>.useItemModel(item: Item) {
            val location = ForgeRegistries.ITEMS.getKey(this.get())!!
            val useItemLocation = ForgeRegistries.ITEMS.getKey(item)!!
            val parentModelLocation = useItemLocation.withPrefix("item/")
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

    class CargoBlockModelProvider(event: GatherDataEvent) :
        BlockModelProvider(event.generator.packOutput, CargoHologram.MOD_ID, event.existingFileHelper) {
        override fun registerModels() {
            AllRegisters.Blocks.apply {
                remoteCraftTable.useBlockModel(Blocks.CRAFTING_TABLE)
                cargoStorage.useBlockModel(Blocks.BARREL)
                cargoInserter.useBlockModel(Blocks.HOPPER)
            }
        }

        private fun RegistryObject<out Block>.useBlockModel(block: Block) {
            val location = ForgeRegistries.BLOCKS.getKey(this.get())!!
            val useBlockLocation = ForgeRegistries.BLOCKS.getKey(block)!!
            val parentModelLocation = useBlockLocation.withPrefix("block/")
            val parentModelFile = ModelFile.ExistingModelFile(parentModelLocation, existingFileHelper)
            getBuilder(location.toString())
                .parent(parentModelFile)
        }

    }

    class CargoBlockStateProvider(event: GatherDataEvent) :
        BlockStateProvider(event.generator.packOutput, CargoHologram.MOD_ID, event.existingFileHelper) {
        override fun registerStatesAndModels() {
            AllRegisters.Blocks.apply {
                remoteCraftTable.selfSingleState()
                cargoStorage.selfSingleState()
                cargoInserter.selfSingleState()
            }
        }

        private fun RegistryObject<out Block>.selfSingleState() {
            val location = ForgeRegistries.BLOCKS.getKey(this.get())!!
            val modelLocation = location.withPrefix("block/")
            val modelFile = models().getExistingFile(modelLocation)
            simpleBlock(this.get(), modelFile)
        }

    }

    class EnglishLangProvider(event: GatherDataEvent) :
        LanguageProvider(event.generator.packOutput, CargoHologram.MOD_ID, "en_us") {
        override fun addTranslations() {
            AllRegisters.Items.apply {
                monitor.lang("Monitor")
                crafter.lang("Crafter")
                cargoFilter.lang("Cargo Filter")
                panel.lang("Monitor Panel")
                traitFilter.lang("Item Trait Filter")
                listFilter.lang("List Filter")
                configureUISTick.lang("Configure UI stick")
                linker.lang("Linker")
            }
            AllRegisters.Blocks.apply {
                remoteCraftTable.lang("RemoteCraftTable")
                cargoStorage.lang("Cargo Storage")
                cargoInserter.lang("Cargo Inserter")
            }
            AllTranslates.apply {
                MOD_TAB.lang(CargoHologram.MOD_NAME)
                CONFIGURE_UI_TIP.lang("use command or Configure UI stick to configure")
            }
        }

        @JvmName("langForItem")
        private fun RegistryObject<out Item>.lang(trans: String) {
            addItem(this, trans)
        }

        @JvmName("lang for Block")
        private fun RegistryObject<out Block>.lang(trans: String) {
            addBlock(this, trans)
        }

        private fun MutableComponent.lang(trans: String) {
            val contents = this.contents
            if (contents is TranslatableContents) {
                add(contents.key, trans)
            } else throw RuntimeException("$this should be TranslateComponent")
        }
    }
}