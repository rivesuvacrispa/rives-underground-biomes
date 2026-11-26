package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayList;
import java.util.List;

public class HangingCoralFeature extends Feature<NoneFeatureConfiguration> {
    private static final Block[][] CORALS = {
        {Blocks.TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL_WALL_FAN},
        {Blocks.BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL_WALL_FAN},
        {Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL_WALL_FAN},
        {Blocks.FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL_WALL_FAN},
        {Blocks.HORN_CORAL_BLOCK, Blocks.HORN_CORAL_WALL_FAN}
    };

    public HangingCoralFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!isWater(level, origin) || isWater(level, origin.above()) || obstructed(level, origin)) return false;
        Block[] coral = CORALS[random.nextInt(CORALS.length)];
        int length = 2 + random.nextInt(4);
        List<BlockPos> placed = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            BlockPos center = origin.below(i);
            if (!isWater(level, center) || !isWater(level, center.below())) break;
            setCoral(level, center, coral[0], placed);
            float spread = 0.8F - i / (float) length;
            Direction open = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (side != open && random.nextFloat() < spread && isWater(level, center.relative(side)))
                    setCoral(level, center.relative(side), coral[0], placed);
            }
        }
        for (BlockPos pos : placed) {
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos fan = pos.relative(side);
                if (random.nextFloat() < 0.35F && isWater(level, fan)) {
                    BlockState state = coral[1].defaultBlockState().setValue(BaseCoralWallFanBlock.FACING, side);
                    Carving.set(level, fan, state);
                }
            }
        }
        return !placed.isEmpty();
    }

    private static boolean obstructed(WorldGenLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -6, -2), origin.offset(2, 1, 2))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlockTags.REEF_STRUCTURE)) return true;
            if (pos.getY() >= origin.getY() - 1 && state.is(BlockTags.CORAL_BLOCKS)) return true;
        }
        return false;
    }

    private static void setCoral(WorldGenLevel level, BlockPos pos, Block block, List<BlockPos> placed) {
        Carving.set(level, pos, block.defaultBlockState());
        placed.add(pos);
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER)
                && level.getFluidState(pos).isSource();
    }
}
