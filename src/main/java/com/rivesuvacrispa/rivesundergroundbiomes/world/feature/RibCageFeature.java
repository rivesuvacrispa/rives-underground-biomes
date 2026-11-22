package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RibCageFeature extends Feature<NoneFeatureConfiguration> {
    public RibCageFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        Direction along = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction across = along.getClockWise();
        int half = 4 + random.nextInt(4);
        int radius = 3 + random.nextInt(3);
        BlockState spine = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, along.getAxis());
        BlockState rib = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        int placed = 0;

        for (int i = -half; i <= half; i++) {
            BlockPos spinePos = origin.relative(along, i);
            if (place(level, spinePos, spine)) placed++;
            if ((i & 1) != 0 || Math.abs(i) >= half - 1) continue;
            double taper = 1 - 0.5 * (i / (double) half) * (i / (double) half);
            double r = radius * taper;
            for (double phi = 0; phi <= Math.PI; phi += 0.12) {
                if (random.nextFloat() < 0.15F) continue;
                if (phi > Math.PI * 0.42 && phi < Math.PI * 0.58 && random.nextBoolean()) continue;
                BlockPos pos = spinePos.relative(across, (int) Math.round(Math.cos(phi) * r))
                        .above((int) Math.round(Math.sin(phi) * r * 1.1));
                if (place(level, pos, rib)) placed++;
            }
        }

        BlockPos skull = origin.relative(along, half + 2);
        for (BlockPos pos : BlockPos.betweenClosed(skull.offset(-1, 0, -1), skull.offset(1, 2, 1))) {
            if (random.nextFloat() < 0.8F && place(level, pos, spine)) placed++;
        }
        return placed > 0;
    }

    private static boolean place(WorldGenLevel level, BlockPos pos, BlockState state) {
        BlockState current = level.getBlockState(pos);
        if (!current.isAir()
                && !current.is(BlockTags.BASE_STONE_OVERWORLD)
                && !current.is(BlockTags.SAND)
                && !current.is(Blocks.SANDSTONE)
                && !current.is(Blocks.GRAVEL)) return false;
        Carving.set(level, pos, state);
        return true;
    }
}
