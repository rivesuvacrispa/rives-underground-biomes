package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// frost crystals grow under ice dripping water
@Mixin(IceBlock.class)
public abstract class IceBlockMixin {
    @Unique
    private static final int GROWTH_CHANCE = 8;

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void rub$growFrostCrystal(
            BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (random.nextInt(GROWTH_CHANCE) != 0
                || !level.getFluidState(pos.above()).is(FluidTags.WATER)) return;
        BlockPos below = pos.below();
        BlockState target = level.getBlockState(below);
        boolean water = target.is(Blocks.WATER) && target.getFluidState().isSource();
        if (!target.isAir() && !water) return;
        level.setBlockAndUpdate(
                below,
                ModBlocks.FROST_CRYSTAL
                        .defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, Direction.DOWN)
                        .setValue(AmethystClusterBlock.WATERLOGGED, water));
    }
}
