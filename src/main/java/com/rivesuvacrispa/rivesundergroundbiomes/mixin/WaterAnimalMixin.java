package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// fish can spawn in all underground reef
@Mixin(WaterAnimal.class)
public abstract class WaterAnimalMixin {
    @ModifyReturnValue(method = "checkSurfaceWaterAnimalSpawnRules", at = @At("RETURN"))
    private static boolean rub$spawnInReef(
            boolean original,
            EntityType<? extends WaterAnimal> type,
            LevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random) {
        return original
                || Zone.REEF.isBiome(level.getBiome(pos))
                        && level.getFluidState(pos.below()).is(FluidTags.WATER)
                        && level.getBlockState(pos.above()).is(Blocks.WATER);
    }
}
