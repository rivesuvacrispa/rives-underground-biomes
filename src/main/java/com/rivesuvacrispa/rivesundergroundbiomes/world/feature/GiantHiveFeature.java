package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class GiantHiveFeature extends Feature<NoneFeatureConfiguration> {
    private static final float HONEY_COVER = 0.4F;

    public GiantHiveFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();
        // radii stay at most 11 so the hive fits the 3x3 chunk feature window
        double rx = 7 + random.nextInt(5);
        double ry = 5 + random.nextInt(3);
        double rz = 7 + random.nextInt(5);
        if (center.getY() - ry < level.getMinBuildHeight() + 5) return false;
        double inner = 1 - 2.0 / ry;
        int tunnelY = (int) (-ry * 0.35) + 2;
        List<BlockPos> hollow = new ArrayList<>();

        for (int dx = (int) -rx; dx <= rx; dx++) {
            for (int dy = (int) -ry; dy <= ry; dy++) {
                for (int dz = (int) -rz; dz <= rz; dz++) {
                    double d = Carving.distance(dx, dy, dz, rx, ry, rz, random);
                    if (d > 1) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!Carving.canOverwrite(level.getBlockState(pos))) continue;
                    if (d >= inner) {
                        Carving.set(level, pos, Blocks.HONEYCOMB_BLOCK.defaultBlockState());
                    } else {
                        Carving.set(level, pos, Blocks.CAVE_AIR.defaultBlockState());
                        hollow.add(pos);
                    }
                }
            }
        }

        for (BlockPos pos : hollow) {
            if (random.nextFloat() < HONEY_COVER && touchesShell(level, pos))
                Carving.set(level, pos, Blocks.HONEY_BLOCK.defaultBlockState());
        }

        int nests = 4 + random.nextInt(4);
        for (int i = 0; i < nests; i++) {
            double angle = random.nextDouble() * Math.PI * 2;
            int dy = random.nextInt((int) ry * 2 - 3) - (int) ry + 2;
            for (double step = 1; step <= Math.max(rx, rz) + 1; step++) {
                BlockPos pos = center.offset(
                        (int) Math.round(Math.cos(angle) * step), dy, (int) Math.round(Math.sin(angle) * step));
                if (level.getBlockState(pos).is(Blocks.HONEYCOMB_BLOCK)) {
                    Carving.placeBeeNest(level, pos, random, 2 + random.nextInt(2));
                    break;
                }
            }
        }

        for (int t = 0; t < 2; t++) {
            double angle = random.nextDouble() * Math.PI * 2;
            for (double step = 0; step <= Math.max(rx, rz) + 2; step++) {
                BlockPos axis =
                        center.offset(Mth.floor(Math.cos(angle) * step), tunnelY, Mth.floor(Math.sin(angle) * step));
                for (BlockPos pos : BlockPos.betweenClosed(axis.offset(-1, 0, -1), axis.offset(1, 1, 1))) {
                    BlockState state = level.getBlockState(pos);
                    if (!state.isAir() && Carving.canOverwrite(state) && !state.is(Blocks.BEE_NEST)) {
                        Carving.set(level, pos, Blocks.CAVE_AIR.defaultBlockState());
                    }
                }
            }
        }
        return true;
    }

    private static boolean touchesShell(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).is(Blocks.HONEYCOMB_BLOCK)) return true;
        }
        return false;
    }
}
