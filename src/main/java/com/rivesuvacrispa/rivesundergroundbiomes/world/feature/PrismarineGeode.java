package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Conversions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

final class PrismarineGeode {
    static final double SHELL = 2;
    private static final float BUDDING = 0.25F;
    private static final float BUDS = 0.35F;

    static void place(
            WorldGenLevel level, BlockPos center, double core, double gap, boolean open, RandomSource random) {
        double radius = core + gap + SHELL;
        int reach = (int) Math.ceil(radius + 0.5);
        List<BlockPos> budding = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dy = -reach; dy <= reach; dy++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    double r = distance + (random.nextDouble() - 0.5) * 0.5;
                    if (distance > core && r > radius) continue;
                    BlockState state = level.getBlockState(pos.setWithOffset(center, dx, dy, dz));
                    if (!Carving.canOverwrite(state)) continue;
                    if (distance <= core) {
                        boolean bud = random.nextFloat() < BUDDING;
                        Carving.set(
                                level,
                                pos,
                                bud
                                        ? ModBlocks.BUDDING_PRISMARINE.defaultBlockState()
                                        : Blocks.PRISMARINE.defaultBlockState());
                        if (bud) budding.add(pos.immutable());
                    } else if (r <= core + gap) {
                        Carving.setWater(level, pos);
                    } else if (!open || !isCave(state)) {
                        Carving.set(
                                level,
                                pos,
                                r <= core + gap + 1
                                        ? Blocks.PRISMARINE.defaultBlockState()
                                        : Blocks.DARK_PRISMARINE.defaultBlockState());
                    }
                }
            }
        }
        for (BlockPos block : budding) growBuds(level, block, random);
    }

    private static void growBuds(WorldGenLevel level, BlockPos budding, RandomSource random) {
        List<Block> stages = ModBlocks.prismarineBuds();
        for (Direction direction : Direction.values()) {
            BlockPos pos = budding.relative(direction);
            if (!isWater(level.getBlockState(pos)) || random.nextFloat() >= BUDS) continue;
            Carving.set(
                    level,
                    pos,
                    stages.get(random.nextInt(stages.size()))
                            .defaultBlockState()
                            .setValue(AmethystClusterBlock.FACING, direction)
                            .setValue(AmethystClusterBlock.WATERLOGGED, true));
        }
    }

    private static boolean isCave(BlockState state) {
        return state.isAir() || !state.getFluidState().isEmpty();
    }

    // underground desert keeps feature water as a marker until it converts the chunk
    private static boolean isWater(BlockState state) {
        return state.is(Blocks.WATER) || state == Conversions.WATER_MARKER;
    }

    private PrismarineGeode() {}
}
