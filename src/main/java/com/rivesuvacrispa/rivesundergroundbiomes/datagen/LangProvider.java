package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LangProvider extends FabricLanguageProvider {
    public LangProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateTranslations(TranslationBuilder builder) {
        builder.add("itemGroup." + RivesUndergroundBiomes.MOD_ID, "Rives Underground Biomes");
        builder.add(biomeKey(Zone.DESERT.biome), "Underground Desert");
        builder.add(biomeKey(Zone.JUNGLE.biome), "Underground Jungle");
        builder.add(biomeKey(Zone.ICE.biome), "Ice Caves");
        builder.add(biomeKey(Zone.REEF.biome), "Underground Reef");
        builder.add(biomeKey(Zone.REEF.deepBiome), "Underground Ocean");
        builder.add(ModBlocks.QUICKSAND, "Quicksand");
        builder.add(ModBlocks.QUICKSAND_BUCKET, "Quicksand Bucket");
        builder.add(ModBlocks.THIN_ICE, "Thin Ice");
        builder.add(ModBlocks.PEARL_SAND, "Pearl Sand");
        ModBlocks.newHosts().forEach(block -> builder.add(block, titleOf(block)));
        ModBlocks.decorations().forEach(block -> builder.add(block, titleOf(block)));
        ModBlocks.permafrostBricks().forEach(block -> builder.add(block, titleOf(block)));
        ModBlocks.pearlstoneBricks().forEach(block -> builder.add(block, titleOf(block)));
        ModBlocks.chests().forEach(block -> builder.add(block, titleOf(block)));
        builder.add(ModEntities.ANT, "Ant");
        builder.add(ModEntities.MUMMY_SKELETON, "Mummy Skeleton");
        builder.add(ModEntities.FROZEN, "Frozen");
        builder.add(ModEntities.JUNGLE_SLIME, "Jungle Slime");
        builder.add(ModEntities.JUNGLE_SPIDER, "Jungle Spider");
        ModEntities.SPAWN_EGGS.forEach(
                egg -> builder.add(egg, title(BuiltInRegistries.ITEM.getKey(egg).getPath())));
        for (Host host : Host.VALUES) {
            for (OreKind kind : OreKind.VALUES) {
                Block ore = ModBlocks.ore(host, kind);
                builder.add(ore, titleOf(ore));
            }
        }
    }

    private static String biomeKey(ResourceKey<Biome> biome) {
        return "biome." + biome.location().getNamespace() + "."
                + biome.location().getPath();
    }

    private static String titleOf(Block block) {
        return title(BuiltInRegistries.BLOCK.getKey(block).getPath());
    }

    private static String title(String id) {
        return Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
