package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import com.rivesuvacrispa.rivesundergroundbiomes.world.ModBiomeTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.ModStructureTags;
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
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

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
            ModBlocks.pearlstoneBricks().forEach(pickaxe::add);
            pickaxe.add(ModBlocks.DESERT_CHEST, ModBlocks.FROST_CHEST, ModBlocks.PRISMARINE_CHEST);
            getOrCreateTagBuilder(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.JUNGLE_CHEST, ModBlocks.SPIDER_CHEST);
            ModBlocks.chests().forEach(chest -> {
                getOrCreateTagBuilder(BlockTags.FEATURES_CANNOT_REPLACE).add(chest);
                getOrCreateTagBuilder(BlockTags.GUARDED_BY_PIGLINS).add(chest);
            });
            getOrCreateTagBuilder(BlockTags.STONE_BRICKS).add(ModBlocks.PERMAFROST_BRICKS);
            getOrCreateTagBuilder(BlockTags.STAIRS).add(ModBlocks.PERMAFROST_BRICK_STAIRS);
            getOrCreateTagBuilder(BlockTags.SLABS).add(ModBlocks.PERMAFROST_BRICK_SLAB);
            getOrCreateTagBuilder(BlockTags.WALLS).add(ModBlocks.PERMAFROST_BRICK_WALL);
            getOrCreateTagBuilder(BlockTags.STONE_BRICKS)
                    .add(ModBlocks.PEARLSTONE_BRICKS, ModBlocks.CHISELED_PEARLSTONE);
            getOrCreateTagBuilder(BlockTags.STAIRS).add(ModBlocks.PEARLSTONE_BRICK_STAIRS);
            getOrCreateTagBuilder(BlockTags.SLABS).add(ModBlocks.PEARLSTONE_BRICK_SLAB);
            getOrCreateTagBuilder(BlockTags.WALLS).add(ModBlocks.PEARLSTONE_BRICK_WALL);
            getOrCreateTagBuilder(BlockTags.STAIRS).add(ModBlocks.SMOOTH_PEARLSTONE_STAIRS);
            getOrCreateTagBuilder(BlockTags.SLABS).add(ModBlocks.SMOOTH_PEARLSTONE_SLAB);
            getOrCreateTagBuilder(BlockTags.WALLS).add(ModBlocks.SMOOTH_PEARLSTONE_WALL);
            FabricTagBuilder bare =
                    getOrCreateTagBuilder(ModBlockTags.REEF_BARE).forceAddTag(BlockTags.BASE_STONE_OVERWORLD);
            for (OreKind kind : OreKind.VALUES) bare.forceAddTag(kind.blockTag);
            FabricTagBuilder structure = getOrCreateTagBuilder(ModBlockTags.REEF_STRUCTURE)
                    .add(ModBlocks.PRISMARINE_CHEST)
                    .add(
                            net.minecraft.world.level.block.Blocks.COPPER_BLOCK,
                            net.minecraft.world.level.block.Blocks.CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.WAXED_COPPER_BLOCK,
                            net.minecraft.world.level.block.Blocks.WAXED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.WAXED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.EXPOSED_COPPER,
                            net.minecraft.world.level.block.Blocks.EXPOSED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.EXPOSED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.EXPOSED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.WAXED_EXPOSED_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_EXPOSED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.WEATHERED_COPPER,
                            net.minecraft.world.level.block.Blocks.WEATHERED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.WEATHERED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.WEATHERED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.WAXED_WEATHERED_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_WEATHERED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.OXIDIZED_COPPER,
                            net.minecraft.world.level.block.Blocks.OXIDIZED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.OXIDIZED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.OXIDIZED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.WAXED_OXIDIZED_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_OXIDIZED_CUT_COPPER,
                            net.minecraft.world.level.block.Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                            net.minecraft.world.level.block.Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB,
                            net.minecraft.world.level.block.Blocks.DARK_PRISMARINE,
                            net.minecraft.world.level.block.Blocks.PRISMARINE_BRICKS,
                            net.minecraft.world.level.block.Blocks.PRISMARINE_BRICK_STAIRS,
                            net.minecraft.world.level.block.Blocks.PRISMARINE_BRICK_SLAB,
                            net.minecraft.world.level.block.Blocks.GLASS,
                            net.minecraft.world.level.block.Blocks.SPAWNER,
                            net.minecraft.world.level.block.Blocks.BARREL,
                            net.minecraft.world.level.block.Blocks.BLAST_FURNACE,
                            net.minecraft.world.level.block.Blocks.REDSTONE_LAMP,
                            net.minecraft.world.level.block.Blocks.LIGHTNING_ROD,
                            net.minecraft.world.level.block.Blocks.LADDER,
                            net.minecraft.world.level.block.Blocks.SPRUCE_STAIRS,
                            net.minecraft.world.level.block.Blocks.DECORATED_POT,
                            net.minecraft.world.level.block.Blocks.GOLD_BLOCK,
                            net.minecraft.world.level.block.Blocks.RAW_GOLD_BLOCK,
                            net.minecraft.world.level.block.Blocks.EMERALD_BLOCK,
                            net.minecraft.world.level.block.Blocks.LAPIS_BLOCK,
                            net.minecraft.world.level.block.Blocks.WET_SPONGE);
            ModBlocks.pearlstoneBricks().forEach(structure::add);
            getOrCreateTagBuilder(BlockTags.ICE).add(ModBlocks.THIN_ICE);
            shovel.add(ModBlocks.PEARL_SAND);
            getOrCreateTagBuilder(BlockTags.SAND).add(ModBlocks.PEARL_SAND);
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
            getOrCreateTagBuilder(Zone.REEF.surfaceTag).add(net.minecraft.world.level.biome.Biomes.WARM_OCEAN);
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
                getOrCreateTagBuilder(c(Registries.BIOME, "caves")).add(zone.biome);
                if (zone != Zone.REEF)
                    getOrCreateTagBuilder(BiomeTags.HAS_MINESHAFT).add(zone.biome);
            }
            getOrCreateTagBuilder(BiomeTags.IS_OVERWORLD).add(Zone.REEF.deepBiome);
            getOrCreateTagBuilder(c(Registries.BIOME, "caves")).add(Zone.REEF.deepBiome);
            getOrCreateTagBuilder(BiomeTags.PLAYS_UNDERWATER_MUSIC).add(Zone.REEF.biome, Zone.REEF.deepBiome);
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

    public static class Structures extends FabricTagProvider<Structure> {
        public Structures(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, Registries.STRUCTURE, registries);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            getOrCreateTagBuilder(ModStructureTags.REEF_AVOIDS)
                    .add(BuiltinStructures.STRONGHOLD, BuiltinStructures.ANCIENT_CITY);
            getOrCreateTagBuilder(ModStructureTags.NOT_IN_REEF)
                    .add(BuiltinStructures.MINESHAFT, BuiltinStructures.MINESHAFT_MESA);
        }
    }

    private TagProviders() {}
}
