package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

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

    private Carving() {}
}
