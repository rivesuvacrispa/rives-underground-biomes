package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.CoralFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// keeps vanilla corals away from reef structures
@Mixin(CoralFeature.class)
public abstract class CoralFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void rub$keepOutOfStructures(
            FeaturePlaceContext<NoneFeatureConfiguration> context, CallbackInfoReturnable<Boolean> cir) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, -1, -4), origin.offset(4, 7, 4))) {
            if (level.getBlockState(pos).is(ModBlockTags.REEF_STRUCTURE)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }
}
