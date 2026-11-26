package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Conversions;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

final class Carving {
    static boolean canOverwrite(BlockState state) {
        return !state.is(BlockTags.FEATURES_CANNOT_REPLACE) && !state.hasBlockEntity();
    }

    static void fillWithBees(WorldGenLevel level, BlockPos pos, int count, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BeehiveBlockEntity hive)) return;
        for (int i = 0; i < count; i++) {
            CompoundTag bee = new CompoundTag();
            bee.putString(
                    "id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE).toString());
            hive.storeBee(bee, random.nextInt(599), false);
        }
    }

    static void placeBeeNest(WorldGenLevel level, BlockPos pos, RandomSource random, int bees) {
        set(
                level,
                pos,
                Blocks.BEE_NEST
                        .defaultBlockState()
                        .setValue(BeehiveBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                        .setValue(BeehiveBlock.HONEY_LEVEL, random.nextInt(6)));
        fillWithBees(level, pos, bees, random);
    }

    // 1 = on the ellipsoid surface
    static double distance(int dx, int dy, int dz, double rx, double ry, double rz, RandomSource random) {
        double d = Math.sqrt(dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz));
        return d + (random.nextDouble() - 0.5) * 0.08;
    }

    static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    static void setWater(WorldGenLevel level, BlockPos pos) {
        boolean converted = level.getChunk(pos).getStatus().isOrAfter(ChunkStatus.FEATURES);
        set(level, pos, converted ? Blocks.WATER.defaultBlockState() : Conversions.WATER_MARKER);
    }

    static void plantUnderwater(WorldGenLevel level, BlockPos pos, int waterAbove, RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.25F) {
            set(level, pos, Blocks.SEAGRASS.defaultBlockState());
        } else if (roll < 0.32F && waterAbove >= 1) {
            tallSeagrass(level, pos);
        } else if (roll < 0.38F && waterAbove >= 2) {
            kelp(level, pos, Math.min(waterAbove, 2 + random.nextInt(8)), random);
        } else if (roll < 0.42F) {
            set(
                    level,
                    pos,
                    Blocks.SEA_PICKLE
                            .defaultBlockState()
                            .setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4))
                            .setValue(SeaPickleBlock.WATERLOGGED, true));
        }
    }

    static void tallSeagrass(WorldGenLevel level, BlockPos pos) {
        set(
                level,
                pos,
                Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
        set(
                level,
                pos.above(),
                Blocks.TALL_SEAGRASS.defaultBlockState().setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
    }

    static void kelp(WorldGenLevel level, BlockPos pos, int length, RandomSource random) {
        for (int i = 0; i < length; i++) {
            set(
                    level,
                    pos.above(i),
                    i == length - 1
                            ? Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 20 + random.nextInt(4))
                            : Blocks.KELP_PLANT.defaultBlockState());
        }
    }

    static void placeTreasureChest(WorldGenLevel level, BlockPos pos, RandomSource random) {
        set(
                level,
                pos,
                ModBlocks.PRISMARINE_CHEST
                        .defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                        .setValue(ChestBlock.WATERLOGGED, true));
        RandomizableContainerBlockEntity.setLootTable(level, random, pos, BuiltInLootTables.BURIED_TREASURE);
    }

    static int findCave(WorldGenLevel level, int x, int z, int top, int bottom, Zone zone) {
        int lowest = Math.max(bottom, level.getMinBuildHeight() + 8);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = top; y >= lowest; y--) {
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isAir()
                    || !level.getBlockState(pos.below()).isAir()) continue;
            if (zone.isBiome(level.getBiome(pos))) return y;
        }
        return Integer.MIN_VALUE;
    }

    // jungle shaft and ant burrow openings stay away from structures
    static boolean nearSurfaceStructure(WorldGenLevel level, int cx, int cz, int reach) {
        for (int chunkX = SectionPos.blockToSectionCoord(cx - reach);
                chunkX <= SectionPos.blockToSectionCoord(cx + reach);
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(cz - reach);
                    chunkZ <= SectionPos.blockToSectionCoord(cz + reach);
                    chunkZ++) {
                for (Structure structure :
                        level.getChunk(chunkX, chunkZ).getAllReferences().keySet()) {
                    if (structure.step() == GenerationStep.Decoration.SURFACE_STRUCTURES) return true;
                }
            }
        }
        return false;
    }

    private Carving() {}
}
