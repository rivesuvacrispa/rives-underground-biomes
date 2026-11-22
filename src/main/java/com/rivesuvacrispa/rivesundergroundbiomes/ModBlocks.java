package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.block.AnthillBlock;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import com.rivesuvacrispa.rivesundergroundbiomes.block.QuicksandBlock;
import com.rivesuvacrispa.rivesundergroundbiomes.block.SmallPlantBlock;
import com.rivesuvacrispa.rivesundergroundbiomes.block.SpeleothemBlock;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ThinIceBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ModBlocks {
    public static final List<Item> ALL_ITEMS = new ArrayList<>();

    public static Block SANDSLATE;
    public static Block MUDSTONE;
    public static Block PERMAFROST;
    public static Block GLACISLATE;
    public static Block PERMAFROST_BRICKS;
    public static Block PERMAFROST_BRICK_STAIRS;
    public static Block PERMAFROST_BRICK_SLAB;
    public static Block PERMAFROST_BRICK_WALL;
    public static Block ICICLE;
    public static Block POINTED_MUD;
    public static Block POINTED_SANDSTONE;
    public static Block FROST_CRYSTAL;
    public static Block GLOWSHROOM;
    public static Block ANTHILL;
    public static Block THIN_ICE;
    public static Block QUICKSAND;
    public static Item QUICKSAND_BUCKET;

    private static final SoundType ANTHILL_SOUND = new SoundType(
            1.0F,
            1.0F,
            SoundEvents.TURTLE_EGG_BREAK,
            SoundEvents.SAND_STEP,
            SoundEvents.SAND_PLACE,
            SoundEvents.SAND_HIT,
            SoundEvents.SAND_FALL);

    private static final Map<Host, EnumMap<OreKind, Block>> ORES = new EnumMap<>(Host.class);

    public static void init() {
        SANDSLATE = register(
                "sandslate",
                new RotatedPillarBlock(Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.SAND)));
        MUDSTONE = register(
                "mudstone",
                new RotatedPillarBlock(Properties.copy(Blocks.DEEPSLATE)
                        .mapColor(MapColor.TERRACOTTA_BROWN)
                        .sound(SoundType.MUD_BRICKS)));
        PERMAFROST =
                register("permafrost", new Block(Properties.copy(Blocks.STONE).mapColor(MapColor.CLAY)));
        GLACISLATE = register(
                "glacislate",
                new RotatedPillarBlock(Properties.copy(Blocks.DEEPSLATE).mapColor(MapColor.ICE)));
        PERMAFROST_BRICKS = register(
                "permafrost_bricks",
                new Block(Properties.copy(Blocks.STONE_BRICKS).mapColor(MapColor.CLAY)));
        PERMAFROST_BRICK_STAIRS = register(
                "permafrost_brick_stairs",
                new StairBlock(PERMAFROST_BRICKS.defaultBlockState(), Properties.copy(PERMAFROST_BRICKS)));
        PERMAFROST_BRICK_SLAB = register("permafrost_brick_slab", new SlabBlock(Properties.copy(PERMAFROST_BRICKS)));
        PERMAFROST_BRICK_WALL = register(
                "permafrost_brick_wall",
                new WallBlock(Properties.copy(PERMAFROST_BRICKS).forceSolidOn()));

        for (Host host : Host.VALUES) {
            EnumMap<OreKind, Block> ores = new EnumMap<>(OreKind.class);
            for (OreKind kind : OreKind.VALUES) {
                Properties props = Properties.copy(kind.vanillaOre(host.deep))
                        .mapColor(host.block().defaultMapColor())
                        .sound(host.oreSound)
                        // vanilla ores are 1.5 harder than their host
                        .strength(host.hardness() + 1.5F, 3.0F);
                Block ore = kind == OreKind.REDSTONE
                        ? new RedStoneOreBlock(props)
                        : new DropExperienceBlock(props, kind.xp);
                ores.put(kind, register(oreName(host, kind), ore));
            }
            ORES.put(host, ores);
        }

        ICICLE = register(
                "icicle",
                new SpeleothemBlock(
                        speleothem(MapColor.ICE, SoundType.GLASS, 0.8F), ParticleTypes.DRIPPING_DRIPSTONE_WATER));
        POINTED_MUD = register(
                "pointed_mud",
                new SpeleothemBlock(
                        speleothem(MapColor.TERRACOTTA_BROWN, SoundType.MUD_BRICKS, 1.0F),
                        ParticleTypes.DRIPPING_DRIPSTONE_WATER));
        POINTED_SANDSTONE = register(
                "pointed_sandstone",
                new SpeleothemBlock(
                        speleothem(MapColor.SAND, SoundType.STONE, 1.2F),
                        new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.SAND.defaultBlockState())));
        FROST_CRYSTAL = register(
                "frost_crystal",
                new AmethystClusterBlock(
                        7,
                        3,
                        Properties.copy(Blocks.AMETHYST_CLUSTER)
                                .mapColor(MapColor.ICE)
                                .strength(0.3F)
                                .lightLevel(state -> 0)));
        GLOWSHROOM = register(
                "glowshroom",
                new SmallPlantBlock(
                        Properties.of()
                                .mapColor(MapColor.COLOR_CYAN)
                                .noCollission()
                                .instabreak()
                                .sound(SoundType.FUNGUS)
                                .lightLevel(state -> 3)
                                .emissiveRendering((state, level, pos) -> true)
                                .offsetType(BlockBehaviour.OffsetType.XZ)
                                .pushReaction(PushReaction.DESTROY),
                        Block.box(4, 0, 4, 12, 9, 12),
                        (ground, level) -> true));
        ANTHILL = registerBlock(
                "anthill",
                new AnthillBlock(
                        Properties.of()
                                .mapColor(MapColor.SAND)
                                .noCollission()
                                .instabreak()
                                .noLootTable()
                                .sound(ANTHILL_SOUND)
                                .offsetType(BlockBehaviour.OffsetType.XZ)
                                .pushReaction(PushReaction.DESTROY),
                        Block.box(2, 0, 2, 14, 7, 14),
                        (ground, level) -> ground.is(ModBlockTags.ANTHILL_PLACEABLE)));

        THIN_ICE = register("thin_ice", new ThinIceBlock(Properties.copy(Blocks.ICE)));

        // no block item like powder snow
        QUICKSAND = Registry.register(
                BuiltInRegistries.BLOCK,
                RivesUndergroundBiomes.id("quicksand"),
                new QuicksandBlock(Properties.copy(Blocks.SAND)
                        .dynamicShape()
                        .isViewBlocking((state, level, pos) -> true)
                        .isSuffocating((state, level, pos) -> false)));
        QUICKSAND_BUCKET = Registry.register(
                BuiltInRegistries.ITEM,
                RivesUndergroundBiomes.id("quicksand_bucket"),
                new SolidBucketItem(QUICKSAND, SoundEvents.SAND_PLACE, new Item.Properties().stacksTo(1)));
        ALL_ITEMS.add(QUICKSAND_BUCKET);
    }

    private static Properties speleothem(MapColor color, SoundType sound, float hardness) {
        return Properties.of()
                .mapColor(color)
                .forceSolidOn()
                .noOcclusion()
                .sound(sound)
                .strength(hardness, 3.0F)
                .dynamicShape()
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.DESTROY)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    public static List<Block> permafrostBricks() {
        return List.of(PERMAFROST_BRICKS, PERMAFROST_BRICK_STAIRS, PERMAFROST_BRICK_SLAB, PERMAFROST_BRICK_WALL);
    }

    public static List<Block> decorations() {
        return List.of(ICICLE, POINTED_MUD, POINTED_SANDSTONE, FROST_CRYSTAL, GLOWSHROOM, ANTHILL);
    }

    public static List<SpeleothemBlock> speleothems() {
        return List.of((SpeleothemBlock) ICICLE, (SpeleothemBlock) POINTED_MUD, (SpeleothemBlock) POINTED_SANDSTONE);
    }

    public static String oreName(Host host, OreKind kind) {
        return host.id + "_" + kind.id + "_ore";
    }

    public static Block ore(Host host, OreKind kind) {
        return ORES.get(host).get(kind);
    }

    // sandstone and mud are vanilla
    public static List<Block> newHosts() {
        return List.of(SANDSLATE, MUDSTONE, PERMAFROST, GLACISLATE);
    }

    private static Block register(String name, Block block) {
        registerBlock(name, block);
        ALL_ITEMS.add(Registry.register(
                BuiltInRegistries.ITEM, RivesUndergroundBiomes.id(name), new BlockItem(block, new Item.Properties())));
        return block;
    }

    private static Block registerBlock(String name, Block block) {
        return Registry.register(BuiltInRegistries.BLOCK, RivesUndergroundBiomes.id(name), block);
    }

    private ModBlocks() {}
}
