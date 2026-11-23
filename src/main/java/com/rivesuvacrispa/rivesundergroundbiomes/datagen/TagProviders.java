package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import com.rivesuvacrispa.rivesundergroundbiomes.world.ModBiomeTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

public final class TagProviders {
    static <T> TagKey<T> c(ResourceKey<? extends Registry<T>> registry, String path) {
        return TagKey.create(registry, new ResourceLocation("c", path));
    }

    static <T> TagKey<T> forge(ResourceKey<? extends Registry<T>> registry, String path) {
        return TagKey.create(registry, new ResourceLocation("forge", path));
    }

    public static class Blocks extends FabricTagProvider.BlockTagProvider {
        public Blocks(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            FabricTagBuilder pickaxe = getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_PICKAXE);
            FabricTagBuilder shovel = getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_SHOVEL);
            FabricTagBuilder ores = getOrCreateTagBuilder(c(Registries.BLOCK, "ores"));
            // hosts count as base stone for moss, sculk and dripstone checks
            ModBlocks.newHosts().forEach(host -> {
                pickaxe.add(host);
                getOrCreateTagBuilder(BlockTags.BASE_STONE_OVERWORLD).add(host);
            });
            for (Host host : Host.VALUES) {
                for (OreKind kind : OreKind.VALUES) {
                    Block ore = ModBlocks.ore(host, kind);
                    // mud ores dig like mud
                    // tool tiers and Fortune work the same with a shovel
                    (host == Host.MUD ? shovel : pickaxe).add(ore);
                    ores.add(ore);
                    getOrCreateTagBuilder(kind.blockTag).add(ore);
                    getOrCreateTagBuilder(c(Registries.BLOCK, kind.id + "_ores"))
                            .add(ore);
                    if (kind.toolTier != null)
                        getOrCreateTagBuilder(kind.toolTier).add(ore);
                }
            }
            pickaxe.add(ModBlocks.ICICLE, ModBlocks.POINTED_SANDSTONE);
            shovel.add(ModBlocks.POINTED_MUD, ModBlocks.QUICKSAND);
            pickaxe.add(ModBlocks.THIN_ICE);
            ModBlocks.permafrostBricks().forEach(pickaxe::add);
            getOrCreateTagBuilder(BlockTags.STONE_BRICKS).add(ModBlocks.PERMAFROST_BRICKS);
            getOrCreateTagBuilder(BlockTags.STAIRS).add(ModBlocks.PERMAFROST_BRICK_STAIRS);
            getOrCreateTagBuilder(BlockTags.SLABS).add(ModBlocks.PERMAFROST_BRICK_SLAB);
            getOrCreateTagBuilder(BlockTags.WALLS).add(ModBlocks.PERMAFROST_BRICK_WALL);
            getOrCreateTagBuilder(BlockTags.ICE).add(ModBlocks.THIN_ICE);
            getOrCreateTagBuilder(ModBlockTags.ROOT_PIERCEABLE)
                    .forceAddTag(BlockTags.BASE_STONE_OVERWORLD)
                    .forceAddTag(BlockTags.DIRT)
                    .add(net.minecraft.world.level.block.Blocks.GRAVEL, net.minecraft.world.level.block.Blocks.CLAY);
            FabricTagBuilder speleothems = getOrCreateTagBuilder(ModBlockTags.SPELEOTHEMS)
                    .add(net.minecraft.world.level.block.Blocks.POINTED_DRIPSTONE);
            ModBlocks.speleothems().forEach(speleothems::add);
            getOrCreateTagBuilder(ModBlockTags.ANTHILL_PLACEABLE)
                    .forceAddTag(BlockTags.SAND)
                    .add(
                            net.minecraft.world.level.block.Blocks.SANDSTONE,
                            net.minecraft.world.level.block.Blocks.RED_SANDSTONE,
                            ModBlocks.SANDSLATE);
        }
    }

    public static class Items extends FabricTagProvider.ItemTagProvider {
        public Items(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries, Blocks blockTags) {
            super(output, registries, blockTags);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            copy(c(Registries.BLOCK, "ores"), c(Registries.ITEM, "ores"));
            for (OreKind kind : OreKind.VALUES) {
                copy(kind.blockTag, kind.itemTag);
                copy(c(Registries.BLOCK, kind.id + "_ores"), c(Registries.ITEM, kind.id + "_ores"));
            }
            copy(BlockTags.STONE_BRICKS, ItemTags.STONE_BRICKS);
            copy(BlockTags.STAIRS, ItemTags.STAIRS);
            copy(BlockTags.SLABS, ItemTags.SLABS);
            copy(BlockTags.WALLS, ItemTags.WALLS);
            // hosts count as cobblestone for stone tools and furnaces like cobbled deepslate
            ModBlocks.newHosts().forEach(host -> {
                getOrCreateTagBuilder(ItemTags.STONE_TOOL_MATERIALS).add(host.asItem());
                getOrCreateTagBuilder(ItemTags.STONE_CRAFTING_MATERIALS).add(host.asItem());
            });
        }
    }

    public static class EntityTypes extends FabricTagProvider.EntityTypeTagProvider {
        public EntityTypes(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            getOrCreateTagBuilder(EntityTypeTags.SKELETONS).add(ModEntities.MUMMY_SKELETON);
            getOrCreateTagBuilder(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(ModEntities.FROZEN);
            getOrCreateTagBuilder(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(ModEntities.FROZEN);
        }
    }

    public static class Biomes extends FabricTagProvider<Biome> {
        public Biomes(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.BIOME, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            getOrCreateTagBuilder(Zone.DESERT.surfaceTag)
                    .add(net.minecraft.world.level.biome.Biomes.DESERT)
                    .addOptionalTag(c(Registries.BIOME, "desert"))
                    .addOptionalTag(c(Registries.BIOME, "is_desert"))
                    .addOptionalTag(forge(Registries.BIOME, "is_desert"));
            getOrCreateTagBuilder(Zone.JUNGLE.surfaceTag)
                    .forceAddTag(BiomeTags.IS_JUNGLE)
                    .addOptionalTag(c(Registries.BIOME, "jungle"))
                    .addOptionalTag(c(Registries.BIOME, "is_jungle"))
                    .addOptionalTag(new ResourceLocation("natures_spirit", "is_tropical_woods"))
                    .addOptional(new ResourceLocation("regions_unexplored", "bamboo_forest"));
            getOrCreateTagBuilder(Zone.ICE.surfaceTag)
                    .add(
                            net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS,
                            net.minecraft.world.level.biome.Biomes.ICE_SPIKES,
                            net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA,
                            net.minecraft.world.level.biome.Biomes.SNOWY_BEACH,
                            net.minecraft.world.level.biome.Biomes.FROZEN_RIVER,
                            net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN,
                            net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN,
                            net.minecraft.world.level.biome.Biomes.GROVE,
                            net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES,
                            net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS,
                            net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS)
                    .addOptionalTag(c(Registries.BIOME, "snowy"))
                    .addOptionalTag(c(Registries.BIOME, "icy"))
                    .addOptionalTag(c(Registries.BIOME, "is_snowy"))
                    .addOptionalTag(c(Registries.BIOME, "is_icy"))
                    .addOptionalTag(forge(Registries.BIOME, "is_snowy"))
                    .addOptional(new ResourceLocation("biomeswevegone", "frosted_taiga"))
                    .addOptional(new ResourceLocation("biomeswevegone", "frosted_coniferous_forest"))
                    .addOptional(new ResourceLocation("natures_spirit", "snowy_redwood_forest"));
            getOrCreateTagBuilder(ModBiomeTags.NO_UNDERGROUND_ZONE)
                    .addOptional(new ResourceLocation("natures_spirit", "snowcapped_red_peaks"));

            getOrCreateTagBuilder(ModBiomeTags.REPLACEABLE_CAVES)
                    .add(
                            net.minecraft.world.level.biome.Biomes.LUSH_CAVES,
                            net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES)
                    .addTag(ModBiomeTags.TERRALITH_CAVES)
                    .addTag(ModBiomeTags.YUNGS_CAVES);
            FabricTagBuilder terralith = getOrCreateTagBuilder(ModBiomeTags.TERRALITH_CAVES);
            for (String cave : new String[] {
                "andesite_caves", "deep_caves", "diorite_caves", "frostfire_caves", "fungal_caves",
                "granite_caves", "infested_caves", "mantle_caves", "thermal_caves", "tuff_caves"
            }) {
                terralith.addOptional(new ResourceLocation("terralith", "cave/" + cave));
            }
            getOrCreateTagBuilder(ModBiomeTags.YUNGS_CAVES)
                    .addOptional(new ResourceLocation("yungscavebiomes", "lost_caves"))
                    .addOptional(new ResourceLocation("yungscavebiomes", "frosted_caves"));
            getOrCreateTagBuilder(ModBiomeTags.INHERITS_NEIGHBOURS)
                    .forceAddTag(BiomeTags.IS_RIVER)
                    .forceAddTag(BiomeTags.IS_BEACH);

            for (Zone zone : Zone.VALUES) {
                getOrCreateTagBuilder(BiomeTags.IS_OVERWORLD).add(zone.biome);
                getOrCreateTagBuilder(BiomeTags.HAS_MINESHAFT).add(zone.biome);
                getOrCreateTagBuilder(c(Registries.BIOME, "caves")).add(zone.biome);
            }
            getOrCreateTagBuilder(BiomeTags.SNOW_GOLEM_MELTS).add(Zone.DESERT.biome);
            getOrCreateTagBuilder(c(Registries.BIOME, "climate_hot")).add(Zone.DESERT.biome, Zone.JUNGLE.biome);
            getOrCreateTagBuilder(c(Registries.BIOME, "climate_dry")).add(Zone.DESERT.biome);
            getOrCreateTagBuilder(c(Registries.BIOME, "climate_wet")).add(Zone.JUNGLE.biome);
            getOrCreateTagBuilder(c(Registries.BIOME, "climate_cold")).add(Zone.ICE.biome);
            getOrCreateTagBuilder(TagKey.create(
                            Registries.BIOME, new ResourceLocation("stoneholm", "has_structure/underground_village")))
                    .add(Zone.JUNGLE.biome, Zone.ICE.biome);
        }
    }

    private TagProviders() {}
}
