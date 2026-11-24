package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import com.rivesuvacrispa.rivesundergroundbiomes.ModFeatures;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.AttachedBlockFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.DungeonFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.NoiseNetFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.SpeleothemFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.UnderwaterGardenFeature;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.CocoaDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RandomOffsetPlacement;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;

import java.util.List;
import java.util.Optional;

// every placed feature belongs to one zone biome so feature order cannot conflict
public final class ModWorldgen {
    // Ice Caves
    static final String ICE_NET = "ice_net";
    static final String ICE_FOSSIL_COAL = "ice_fossil_coal";
    static final String ICE_FOSSIL_DIAMONDS = "ice_fossil_diamonds";
    static final String PACKED_ICE_POCKET = "packed_ice_pocket";
    static final String BLUE_ICE_POCKET = "blue_ice_pocket";
    static final String ICICLES = "icicles";
    static final String FROST_CRYSTALS = "frost_crystals";
    static final String SNOW_PATCH = "snow_patch";
    static final String ICE_DUNGEON = "ice_dungeon";
    // Underground Jungle
    static final String GIANT_HIVE = "giant_hive";
    static final String SPIDER_NEST = "spider_nest";
    static final String MUD_SPELEOTHEMS = "mud_speleothems";
    static final String MUD_POOL = "mud_pool";
    static final String JUNGLE_FLOOR = "jungle_floor";
    static final String JUNGLE_TREES = "jungle_trees";
    static final String ROOT_TANGLE = "root_tangle";
    static final String GLOWSHROOMS = "glowshrooms";
    static final String CAVE_VINES = "cave_vines";
    static final String SPORE_BLOSSOMS = "spore_blossoms";
    static final String WALL_VINES = "wall_vines";
    static final String GLOW_LICHEN = "glow_lichen";
    static final String BEE_NEST = "bee_nest";
    static final String ROOT_NET = "root_net";
    static final String BIG_JUNGLE_TREES = "big_jungle_trees";
    static final String MOSS_COAT = "moss_coat";
    static final String MOSS_CEILING = "moss_ceiling";
    static final String VINE_CURTAINS = "vine_curtains";
    static final String JUNGLE_WATER_GARDEN = "jungle_water_garden";
    static final String JUNGLE_DUNGEON = "jungle_dungeon";
    static final String JUNGLE_SHAFT = "jungle_shaft";
    // Underground Desert
    static final String FOSSIL_COAL = "desert_fossil_coal";
    static final String FOSSIL_DIAMONDS = "desert_fossil_diamonds";
    static final String RIB_CAGE = "rib_cage";
    static final String SAND_POCKET = "sand_pocket";
    static final String SANDSTONE_SPELEOTHEMS = "sandstone_speleothems";
    static final String SAND_PATCH = "sand_patch";
    static final String BURIED_RELIC = "buried_relic";
    static final String DESERT_DUNGEON = "desert_dungeon";

    private static final int DUNGEON_ATTEMPTS = 6;
    private static final PlacementModifier RANGE =
            HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.absolute(80));
    // no trees or plants on stalagmites
    private static final BlockPredicate FLOOR = BlockPredicate.allOf(
            BlockPredicate.solid(), BlockPredicate.not(BlockPredicate.matchesTag(ModBlockTags.SPELEOTHEMS)));

    public static void configured(BootstapContext<ConfiguredFeature<?, ?>> context) {
        HolderGetter<ConfiguredFeature<?, ?>> vanilla = context.lookup(Registries.CONFIGURED_FEATURE);

        // ice tubes about 6 blocks thick
        FeatureUtils.register(
                context,
                configuredKey(ICE_NET),
                ModFeatures.NOISE_NET,
                new NoiseNetFeature.Config(
                        List.of(ModBlocks.THIN_ICE.defaultBlockState()),
                        Zone.ICE,
                        1 / 32F,
                        0.7F,
                        0.1F,
                        101,
                        Optional.empty(),
                        Optional.empty(),
                        0F));
        FeatureUtils.register(
                context,
                configuredKey(PACKED_ICE_POCKET),
                Feature.ORE,
                pocket(Blocks.PACKED_ICE.defaultBlockState(), 33));
        FeatureUtils.register(
                context, configuredKey(BLUE_ICE_POCKET), Feature.ORE, pocket(Blocks.BLUE_ICE.defaultBlockState(), 10));
        FeatureUtils.register(
                context,
                configuredKey(ICICLES),
                ModFeatures.SPELEOTHEM,
                new SpeleothemFeature.Config(ModBlocks.ICICLE, 20, 6, UniformInt.of(1, 5), 0.65F, 0.3F));
        FeatureUtils.register(
                context,
                configuredKey(FROST_CRYSTALS),
                ModFeatures.ATTACHED_BLOCK,
                new AttachedBlockFeature.Config(ModBlocks.FROST_CRYSTAL.defaultBlockState(), 24, 5));
        FeatureUtils.register(
                context,
                configuredKey(SNOW_PATCH),
                Feature.VEGETATION_PATCH,
                new VegetationPatchConfiguration(
                        BlockTags.MOSS_REPLACEABLE,
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.SNOW_BLOCK.defaultBlockState(), 8)
                                .add(Blocks.POWDER_SNOW.defaultBlockState(), 1)
                                .add(Blocks.PACKED_ICE.defaultBlockState(), 2)),
                        PlacementUtils.inlinePlaced(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(weighted(SimpleWeightedRandomList.<BlockState>builder()
                                        .add(Blocks.SNOW.defaultBlockState(), 3)
                                        .add(Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 2), 1)))),
                        CaveSurface.FLOOR,
                        ConstantInt.of(1),
                        0.0F,
                        5,
                        0.4F,
                        UniformInt.of(2, 4),
                        0.3F));
        FeatureUtils.register(
                context,
                configuredKey(ICE_DUNGEON),
                ModFeatures.DUNGEON,
                new DungeonFeature.Config(
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(ModBlocks.PERMAFROST_BRICKS.defaultBlockState(), 4)
                                .add(Blocks.PACKED_ICE.defaultBlockState(), 1)),
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(ModBlocks.PERMAFROST_BRICKS.defaultBlockState(), 2)
                                .add(ModBlocks.PERMAFROST.defaultBlockState(), 1)
                                .add(Blocks.PACKED_ICE.defaultBlockState(), 1)),
                        List.of(ModEntities.FROZEN, EntityType.STRAY)));

        FeatureUtils.register(context, configuredKey(GIANT_HIVE), ModFeatures.GIANT_HIVE);
        FeatureUtils.register(context, configuredKey(SPIDER_NEST), ModFeatures.SPIDER_NEST);
        FeatureUtils.register(
                context,
                configuredKey(MUD_SPELEOTHEMS),
                ModFeatures.SPELEOTHEM,
                new SpeleothemFeature.Config(
                        ModBlocks.POINTED_MUD,
                        16,
                        6,
                        UniformInt.of(1, 4),
                        0.55F,
                        0.35F,
                        Optional.of(Blocks.HANGING_ROOTS.defaultBlockState())));
        FeatureUtils.register(
                context,
                configuredKey(MUD_POOL),
                Feature.WATERLOGGED_VEGETATION_PATCH,
                new VegetationPatchConfiguration(
                        BlockTags.MOSS_REPLACEABLE,
                        BlockStateProvider.simple(Blocks.MUD),
                        PlacementUtils.inlinePlaced(vanilla.getOrThrow(CaveFeatures.DRIPLEAF)),
                        CaveSurface.FLOOR,
                        ConstantInt.of(3),
                        0.8F,
                        5,
                        0.1F,
                        UniformInt.of(4, 7),
                        0.7F));
        FeatureUtils.register(
                context,
                configuredKey(JUNGLE_FLOOR),
                Feature.VEGETATION_PATCH,
                new VegetationPatchConfiguration(
                        BlockTags.MOSS_REPLACEABLE,
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.GRASS_BLOCK.defaultBlockState(), 6)
                                .add(Blocks.MOSS_BLOCK.defaultBlockState(), 2)),
                        PlacementUtils.inlinePlaced(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(weighted(SimpleWeightedRandomList.<BlockState>builder()
                                        .add(Blocks.GRASS.defaultBlockState(), 30)
                                        .add(Blocks.FERN.defaultBlockState(), 20)
                                        .add(Blocks.LARGE_FERN.defaultBlockState(), 6)
                                        .add(Blocks.TALL_GRASS.defaultBlockState(), 5)
                                        .add(Blocks.AZALEA.defaultBlockState(), 3)
                                        .add(Blocks.FLOWERING_AZALEA.defaultBlockState(), 2)
                                        .add(Blocks.BLUE_ORCHID.defaultBlockState(), 3)
                                        .add(Blocks.POPPY.defaultBlockState(), 3)
                                        .add(Blocks.DANDELION.defaultBlockState(), 2)
                                        .add(Blocks.ALLIUM.defaultBlockState(), 1)))),
                        CaveSurface.FLOOR,
                        ConstantInt.of(1),
                        0.0F,
                        5,
                        0.6F,
                        UniformInt.of(2, 5),
                        0.3F));
        FeatureUtils.register(
                context,
                configuredKey(JUNGLE_TREES),
                Feature.RANDOM_SELECTOR,
                new RandomFeatureConfiguration(
                        List.of(
                                new WeightedPlacedFeature(
                                        PlacementUtils.inlinePlaced(vanilla.getOrThrow(TreeFeatures.JUNGLE_TREE)),
                                        0.35F),
                                new WeightedPlacedFeature(
                                        PlacementUtils.inlinePlaced(vanilla.getOrThrow(TreeFeatures.MANGROVE)), 0.25F)),
                        PlacementUtils.inlinePlaced(vanilla.getOrThrow(TreeFeatures.JUNGLE_BUSH))));
        FeatureUtils.register(context, configuredKey(ROOT_TANGLE), ModFeatures.ROOT_TANGLE);
        FeatureUtils.register(
                context,
                configuredKey(GLOWSHROOMS),
                Feature.RANDOM_PATCH,
                new RandomPatchConfiguration(
                        24,
                        5,
                        2,
                        PlacementUtils.onlyWhenEmpty(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(BlockStateProvider.simple(ModBlocks.GLOWSHROOM)))));
        FeatureUtils.register(context, configuredKey(BEE_NEST), ModFeatures.BEE_NEST);
        // jungle wood roots about 2 blocks thick
        FeatureUtils.register(
                context,
                configuredKey(ROOT_NET),
                ModFeatures.NOISE_NET,
                new NoiseNetFeature.Config(
                        List.of(Blocks.JUNGLE_WOOD
                                .defaultBlockState()
                                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)),
                        Zone.JUNGLE,
                        1 / 24F,
                        0.6F,
                        0.043F,
                        202,
                        Optional.of(ModBlockTags.ROOT_PIERCEABLE),
                        Optional.of(Blocks.HANGING_ROOTS.defaultBlockState()),
                        0.3F));
        // the vanilla mega jungle tree is 10-31 tall and never fits a cave
        FeatureUtils.register(
                context,
                configuredKey(BIG_JUNGLE_TREES),
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                                BlockStateProvider.simple(Blocks.JUNGLE_LOG),
                                new MegaJungleTrunkPlacer(8, 2, 5),
                                BlockStateProvider.simple(Blocks.JUNGLE_LEAVES),
                                new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                                new TwoLayersFeatureSize(1, 1, 2))
                        .decorators(List.of(
                                TrunkVineDecorator.INSTANCE, new LeaveVineDecorator(0.25F), new CocoaDecorator(0.2F)))
                        .build());
        FeatureUtils.register(context, configuredKey(MOSS_COAT), ModFeatures.MOSS_COAT);
        FeatureUtils.register(context, configuredKey(VINE_CURTAINS), ModFeatures.VINE_CURTAIN);
        FeatureUtils.register(
                context,
                configuredKey(JUNGLE_WATER_GARDEN),
                ModFeatures.UNDERWATER_GARDEN,
                new UnderwaterGardenFeature.Config(24, 6, 0.45F, 0.2F, 0.3F, 0.05F));
        FeatureUtils.register(
                context,
                configuredKey(JUNGLE_DUNGEON),
                ModFeatures.DUNGEON,
                new DungeonFeature.Config(
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.MUD_BRICKS.defaultBlockState(), 5)
                                .add(Blocks.PACKED_MUD.defaultBlockState(), 1)),
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.MUD_BRICKS.defaultBlockState(), 2)
                                .add(Blocks.PACKED_MUD.defaultBlockState(), 1)
                                .add(Blocks.MOSS_BLOCK.defaultBlockState(), 1)),
                        List.of(ModEntities.JUNGLE_SLIME)));
        FeatureUtils.register(context, configuredKey(JUNGLE_SHAFT), ModFeatures.JUNGLE_SHAFT);

        FeatureUtils.register(context, configuredKey(RIB_CAGE), ModFeatures.RIB_CAGE);
        FeatureUtils.register(
                context, configuredKey(SAND_POCKET), Feature.ORE, pocket(Blocks.SAND.defaultBlockState(), 40));
        FeatureUtils.register(
                context,
                configuredKey(SANDSTONE_SPELEOTHEMS),
                ModFeatures.SPELEOTHEM,
                new SpeleothemFeature.Config(ModBlocks.POINTED_SANDSTONE, 16, 6, UniformInt.of(1, 4), 0.55F, 0.35F));
        FeatureUtils.register(
                context,
                configuredKey(SAND_PATCH),
                Feature.VEGETATION_PATCH,
                new VegetationPatchConfiguration(
                        BlockTags.MOSS_REPLACEABLE,
                        BlockStateProvider.simple(Blocks.SAND),
                        PlacementUtils.inlinePlaced(
                                Feature.SIMPLE_BLOCK,
                                new SimpleBlockConfiguration(weighted(SimpleWeightedRandomList.<BlockState>builder()
                                        .add(Blocks.DEAD_BUSH.defaultBlockState(), 3)
                                        .add(ModBlocks.ANTHILL.defaultBlockState(), 2)))),
                        CaveSurface.FLOOR,
                        UniformInt.of(1, 2),
                        0.0F,
                        5,
                        0.2F,
                        UniformInt.of(2, 5),
                        0.3F));
        FeatureUtils.register(context, configuredKey(BURIED_RELIC), ModFeatures.BURIED_RELIC);
        FeatureUtils.register(
                context,
                configuredKey(DESERT_DUNGEON),
                ModFeatures.DUNGEON,
                new DungeonFeature.Config(
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.CUT_SANDSTONE.defaultBlockState(), 4)
                                .add(Blocks.SANDSTONE.defaultBlockState(), 3)
                                .add(Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 2)
                                .add(Blocks.CHISELED_SANDSTONE.defaultBlockState(), 1)),
                        weighted(SimpleWeightedRandomList.<BlockState>builder()
                                .add(Blocks.SMOOTH_SANDSTONE.defaultBlockState(), 3)
                                .add(Blocks.CUT_SANDSTONE.defaultBlockState(), 1)),
                        List.of(EntityType.HUSK, ModEntities.MUMMY_SKELETON)));
    }

    public static void placed(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);

        place(context, configured, ICE_NET, List.of());
        PlacementUtils.register(
                context,
                placedKey(ICE_FOSSIL_COAL),
                configured.getOrThrow(CaveFeatures.FOSSIL_COAL),
                rare(10, HeightRangePlacement.uniform(VerticalAnchor.absolute(-56), VerticalAnchor.absolute(40))));
        PlacementUtils.register(
                context,
                placedKey(ICE_FOSSIL_DIAMONDS),
                configured.getOrThrow(CaveFeatures.FOSSIL_DIAMONDS),
                rare(20, HeightRangePlacement.uniform(VerticalAnchor.absolute(-56), VerticalAnchor.absolute(0))));
        place(context, configured, PACKED_ICE_POCKET, scattered(8));
        place(context, configured, BLUE_ICE_POCKET, scattered(3));
        place(context, configured, ICICLES, scattered(24));
        place(context, configured, FROST_CRYSTALS, scattered(6));
        place(context, configured, SNOW_PATCH, floor(40));
        place(context, configured, ICE_DUNGEON, scattered(DUNGEON_ATTEMPTS));

        place(
                context,
                configured,
                GIANT_HIVE,
                rare(20, HeightRangePlacement.uniform(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(30))));
        place(
                context,
                configured,
                SPIDER_NEST,
                rare(14, HeightRangePlacement.uniform(VerticalAnchor.absolute(-50), VerticalAnchor.absolute(40))));
        place(context, configured, MUD_SPELEOTHEMS, scattered(20));
        place(context, configured, MUD_POOL, floor(10));
        place(context, configured, JUNGLE_FLOOR, floor(60));
        place(context, configured, JUNGLE_TREES, floor(40));
        place(context, configured, ROOT_TANGLE, ceiling(8));
        place(context, configured, GLOWSHROOMS, floor(14));
        PlacementUtils.register(
                context, placedKey(CAVE_VINES), configured.getOrThrow(CaveFeatures.CAVE_VINE), ceiling(60));
        PlacementUtils.register(
                context, placedKey(SPORE_BLOSSOMS), configured.getOrThrow(CaveFeatures.SPORE_BLOSSOM), ceiling(12));
        PlacementUtils.register(
                context, placedKey(WALL_VINES), configured.getOrThrow(VegetationFeatures.VINES), scattered(200));
        PlacementUtils.register(
                context, placedKey(GLOW_LICHEN), configured.getOrThrow(CaveFeatures.GLOW_LICHEN), scattered(70));
        place(context, configured, BEE_NEST, ceiling(3));
        place(context, configured, ROOT_NET, List.of());
        place(context, configured, BIG_JUNGLE_TREES, floor(40));
        place(context, configured, MOSS_COAT, scattered(30));
        PlacementUtils.register(
                context, placedKey(MOSS_CEILING), configured.getOrThrow(CaveFeatures.MOSS_PATCH_CEILING), ceiling(25));
        place(context, configured, VINE_CURTAINS, scattered(40));
        place(context, configured, JUNGLE_WATER_GARDEN, waterFloor(80));
        place(context, configured, JUNGLE_DUNGEON, scattered(DUNGEON_ATTEMPTS));
        place(context, configured, JUNGLE_SHAFT, List.of(RarityFilter.onAverageOnceEvery(16)));

        PlacementUtils.register(
                context,
                placedKey(FOSSIL_COAL),
                configured.getOrThrow(CaveFeatures.FOSSIL_COAL),
                rare(10, HeightRangePlacement.uniform(VerticalAnchor.absolute(-56), VerticalAnchor.absolute(40))));
        PlacementUtils.register(
                context,
                placedKey(FOSSIL_DIAMONDS),
                configured.getOrThrow(CaveFeatures.FOSSIL_DIAMONDS),
                rare(20, HeightRangePlacement.uniform(VerticalAnchor.absolute(-56), VerticalAnchor.absolute(0))));
        place(
                context,
                configured,
                RIB_CAGE,
                List.of(
                        RarityFilter.onAverageOnceEvery(4),
                        InSquarePlacement.spread(),
                        RANGE,
                        EnvironmentScanPlacement.scanningFor(
                                Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                        RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                        BiomeFilter.biome()));
        place(context, configured, SAND_POCKET, scattered(10));
        place(context, configured, SANDSTONE_SPELEOTHEMS, scattered(20));
        place(context, configured, SAND_PATCH, floor(30));
        place(context, configured, BURIED_RELIC, floor(8));
        place(context, configured, DESERT_DUNGEON, scattered(DUNGEON_ATTEMPTS));
    }

    public static void addIceCaves(BiomeGenerationSettings.Builder generation) {
        add(generation, GenerationStep.Decoration.LOCAL_MODIFICATIONS, ICE_NET);
        add(
                generation,
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                ICE_FOSSIL_COAL,
                ICE_FOSSIL_DIAMONDS,
                ICE_DUNGEON);
        add(generation, GenerationStep.Decoration.UNDERGROUND_ORES, PACKED_ICE_POCKET, BLUE_ICE_POCKET);
        add(generation, GenerationStep.Decoration.UNDERGROUND_DECORATION, ICICLES, FROST_CRYSTALS, SNOW_PATCH);
    }

    public static void addUndergroundJungle(BiomeGenerationSettings.Builder generation) {
        add(generation, GenerationStep.Decoration.LOCAL_MODIFICATIONS, ROOT_NET, JUNGLE_SHAFT);
        add(generation, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, GIANT_HIVE, SPIDER_NEST, JUNGLE_DUNGEON);
        add(generation, GenerationStep.Decoration.UNDERGROUND_DECORATION, MUD_SPELEOTHEMS, MOSS_COAT);
        add(
                generation,
                GenerationStep.Decoration.VEGETAL_DECORATION,
                MUD_POOL,
                JUNGLE_FLOOR,
                BIG_JUNGLE_TREES,
                JUNGLE_TREES,
                ROOT_TANGLE,
                GLOWSHROOMS,
                MOSS_CEILING,
                CAVE_VINES,
                SPORE_BLOSSOMS,
                WALL_VINES,
                VINE_CURTAINS,
                GLOW_LICHEN,
                BEE_NEST,
                JUNGLE_WATER_GARDEN);
    }

    public static void addUndergroundDesert(BiomeGenerationSettings.Builder generation) {
        add(
                generation,
                GenerationStep.Decoration.UNDERGROUND_STRUCTURES,
                FOSSIL_COAL,
                FOSSIL_DIAMONDS,
                RIB_CAGE,
                DESERT_DUNGEON);
        add(generation, GenerationStep.Decoration.UNDERGROUND_ORES, SAND_POCKET);
        add(
                generation,
                GenerationStep.Decoration.UNDERGROUND_DECORATION,
                SANDSTONE_SPELEOTHEMS,
                SAND_PATCH,
                BURIED_RELIC);
    }

    private static void add(
            BiomeGenerationSettings.Builder generation, GenerationStep.Decoration step, String... names) {
        for (String name : names) generation.addFeature(step, placedKey(name));
    }

    private static void place(
            BootstapContext<PlacedFeature> context,
            HolderGetter<ConfiguredFeature<?, ?>> configured,
            String name,
            List<PlacementModifier> modifiers) {
        PlacementUtils.register(context, placedKey(name), configured.getOrThrow(configuredKey(name)), modifiers);
    }

    // the feature looks for space itself
    private static List<PlacementModifier> scattered(int count) {
        return List.of(CountPlacement.of(count), InSquarePlacement.spread(), RANGE, BiomeFilter.biome());
    }

    private static List<PlacementModifier> floor(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                RANGE,
                EnvironmentScanPlacement.scanningFor(Direction.DOWN, FLOOR, BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                BiomeFilter.biome());
    }

    private static List<PlacementModifier> ceiling(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                RANGE,
                EnvironmentScanPlacement.scanningFor(
                        Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
                RandomOffsetPlacement.vertical(ConstantInt.of(-1)),
                BiomeFilter.biome());
    }

    private static List<PlacementModifier> waterFloor(int count) {
        return List.of(
                CountPlacement.of(count),
                InSquarePlacement.spread(),
                RANGE,
                EnvironmentScanPlacement.scanningFor(
                        Direction.DOWN, FLOOR, BlockPredicate.matchesFluids(Fluids.WATER), 16),
                RandomOffsetPlacement.vertical(ConstantInt.of(1)),
                BiomeFilter.biome());
    }

    private static List<PlacementModifier> rare(int chunks, PlacementModifier height) {
        return List.of(
                RarityFilter.onAverageOnceEvery(chunks), InSquarePlacement.spread(), height, BiomeFilter.biome());
    }

    private static OreConfiguration pocket(BlockState state, int size) {
        return new OreConfiguration(new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD), state, size);
    }

    private static WeightedStateProvider weighted(SimpleWeightedRandomList.Builder<BlockState> list) {
        return new WeightedStateProvider(list);
    }

    static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, RivesUndergroundBiomes.id(name));
    }

    static ResourceKey<PlacedFeature> placedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, RivesUndergroundBiomes.id(name));
    }

    private ModWorldgen() {}
}
