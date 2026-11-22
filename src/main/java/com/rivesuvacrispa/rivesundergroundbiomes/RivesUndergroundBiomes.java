package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Conversions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
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
    }
}
