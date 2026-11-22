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

public class RootTangleFeature extends Feature<NoneFeatureConfiguration> {
    public RootTangleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!level.isEmptyBlock(origin) || level.isEmptyBlock(origin.above())) return false;

        int capRadius = 1 + random.nextInt(2);
        for (int dx = -capRadius - 1; dx <= capRadius + 1; dx++) {
            for (int dz = -capRadius - 1; dz <= capRadius + 1; dz++) {
                if (dx * dx + dz * dz > capRadius * capRadius + random.nextInt(3)) continue;
                BlockPos ceiling = ceilingAbove(level, origin.offset(dx, 0, dz));
                if (ceiling == null) continue;
                Carving.set(level, ceiling, Blocks.ROOTED_DIRT.defaultBlockState());
                if (level.isEmptyBlock(ceiling.below()) && random.nextFloat() < 0.6F) {
                    Carving.set(level, ceiling.below(), Blocks.HANGING_ROOTS.defaultBlockState());
                }
            }
        }

        int strands = 2 + random.nextInt(3);
        for (int s = 0; s < strands; s++) {
            BlockPos.MutableBlockPos pos = origin.offset(random.nextInt(3) - 1, 0, random.nextInt(3) - 1)
                    .mutable();
            int length = 5 + random.nextInt(8);
            for (int i = 0; i < length; i++) {
                BlockState here = level.getBlockState(pos);
                if (!here.isAir() && !here.is(Blocks.HANGING_ROOTS)) break;
                Carving.set(
                        level,
                        pos,
                        random.nextFloat() < 0.25F
                                ? Blocks.JUNGLE_WOOD
                                        .defaultBlockState()
                                        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                                : Blocks.MANGROVE_ROOTS.defaultBlockState());
                BlockPos next = random.nextFloat() < 0.7F
                        ? pos.below()
                        : pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
                if (!level.isEmptyBlock(next)) {
                    if (level.isEmptyBlock(pos.below()))
                        Carving.set(level, pos.below(), Blocks.HANGING_ROOTS.defaultBlockState());
                    break;
                }
                pos.set(next);
            }
        }
        return true;
    }

    private static BlockPos ceilingAbove(WorldGenLevel level, BlockPos pos) {
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos check = pos.above(dy);
            BlockState state = level.getBlockState(check);
            if (state.isAir()) continue;
            return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(BlockTags.DIRT) ? check : null;
        }
        return null;
    }
}
