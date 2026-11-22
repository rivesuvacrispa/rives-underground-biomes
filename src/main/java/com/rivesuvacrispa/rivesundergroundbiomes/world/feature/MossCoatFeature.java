package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MossCoatFeature extends Feature<NoneFeatureConfiguration> {
    public MossCoatFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = 3 + random.nextInt(3);
        boolean placed = false;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.offset(-radius, -radius, -radius), origin.offset(radius, radius, radius))) {
            if (pos.distSqr(origin) > radius * radius || random.nextFloat() > 0.75F) continue;
            if (!level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD) || !touchesAir(level, pos)) continue;
            Carving.set(level, pos, Blocks.MOSS_BLOCK.defaultBlockState());
            placed = true;
        }
        return placed;
    }

    private static boolean touchesAir(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.isEmptyBlock(pos.relative(direction))) return true;
        }
        return false;
    }
}
