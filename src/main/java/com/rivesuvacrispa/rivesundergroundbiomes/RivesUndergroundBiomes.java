package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Conversions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RivesUndergroundBiomes implements ModInitializer {
    public static final String MOD_ID = "rives_underground_biomes";
    public static final Logger LOGGER = LoggerFactory.getLogger("Rives' Underground Biomes");

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModFeatures.init();
        Conversions.build();
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                id("main"),
                FabricItemGroup.builder()
                        .title(Component.translatable("itemGroup." + MOD_ID))
                        .icon(() -> new ItemStack(ModBlocks.SANDSLATE))
                        .displayItems((params, output) -> ModBlocks.ALL_ITEMS.forEach(output::accept))
                        .build());

        // Built in resource packs to make other cave mods overwrite ours
        ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        ResourceManagerHelper.registerBuiltinResourcePack(
                id("keep_terralith_caves"), mod, "Terralith caves overwrite Rives'", ResourcePackActivationType.NORMAL);
        ResourceManagerHelper.registerBuiltinResourcePack(
                id("keep_yungs_caves"), mod, "YUNG's caves overwrite Rives'", ResourcePackActivationType.NORMAL);
    }
}
