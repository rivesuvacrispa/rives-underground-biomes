package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class VineCurtainFeature extends Feature<NoneFeatureConfiguration> {
    public VineCurtainFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean placed = false;
        for (int i = 0; i < 12; i++) {
            BlockPos pos =
                    context.origin().offset(random.nextInt(13) - 6, random.nextInt(9) - 4, random.nextInt(13) - 6);
            if (!level.isEmptyBlock(pos)) continue;
            for (Direction toWall : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
                if (!wall(level, pos, toWall)) continue;
                BlockPos.MutableBlockPos cursor = pos.mutable();
                for (int up = 0;
                        up < 8 && level.isEmptyBlock(cursor.above()) && wall(level, cursor.above(), toWall);
                        up++) cursor.move(Direction.UP);
                BlockState vine = Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(toWall), true);
                int length = 3 + random.nextInt(10);
                for (int j = 0; j < length && level.isEmptyBlock(cursor); j++) {
                    Carving.set(level, cursor, vine);
                    cursor.move(Direction.DOWN);
                }
                placed = true;
                break;
            }
        }
        return placed;
    }

    private static boolean wall(WorldGenLevel level, BlockPos pos, Direction toWall) {
        BlockPos wall = pos.relative(toWall);
        return level.getBlockState(wall).isFaceSturdy(level, wall, toWall.getOpposite());
    }
}
