package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BuddingPrismarineBlock extends Block {
    private static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingPrismarineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) != 0) return;
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos target = pos.relative(direction);
        Block next = nextStage(level.getBlockState(target), direction);
        if (next == null) return;
        level.setBlockAndUpdate(
                target,
                next.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, true));
    }

    @Nullable
    private static Block nextStage(BlockState state, Direction direction) {
        if (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8) return ModBlocks.SMALL_PRISMARINE_BUD;
        List<Block> stages = ModBlocks.prismarineBuds();
        int stage = stages.indexOf(state.getBlock());
        if (stage < 0 || stage == stages.size() - 1) return null;
        if (state.getValue(AmethystClusterBlock.FACING) != direction
                || !state.getValue(AmethystClusterBlock.WATERLOGGED)) return null;
        return stages.get(stage + 1);
    }
}
