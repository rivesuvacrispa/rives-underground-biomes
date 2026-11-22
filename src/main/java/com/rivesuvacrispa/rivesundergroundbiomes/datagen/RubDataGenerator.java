package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;

public class RubDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        pack.addProvider(BiomeProvider::new);
        pack.addProvider(ModelProvider::new);
        pack.addProvider(LootProvider::new);
        TagProviders.Blocks blockTags = pack.addProvider(TagProviders.Blocks::new);
        pack.addProvider((output, registries) -> new TagProviders.Items(output, registries, blockTags));
        pack.addProvider(TagProviders.Biomes::new);
        pack.addProvider(TagProviders.EntityTypes::new);
        pack.addProvider(RecipeProvider::new);
        pack.addProvider(LangProvider::new);
        pack.addProvider(CreateCrushingProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.CONFIGURED_FEATURE, ModWorldgen::configured);
        registryBuilder.add(Registries.PLACED_FEATURE, ModWorldgen::placed);
        registryBuilder.add(Registries.BIOME, BiomeProvider::bootstrap);
    }
}
