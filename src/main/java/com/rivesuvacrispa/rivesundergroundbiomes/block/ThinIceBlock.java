package com.rivesuvacrispa.rivesundergroundbiomes.block;

import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ThinIceBlock extends IceBlock {
    public ThinIceBlock(Properties properties) {
        super(properties);
    }

    // copy of playerDestroy the IceBlock one places water
    @Override
    public void playerDestroy(
            Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005F);
        dropResources(state, level, pos, blockEntity, player, tool);
    }

    @Override
    protected void melt(BlockState state, Level level, BlockPos pos) {
        level.removeBlock(pos, false);
    }
}
