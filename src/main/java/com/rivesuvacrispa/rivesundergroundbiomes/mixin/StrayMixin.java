package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Stray;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Stray.class)
public abstract class StrayMixin {
    @Redirect(
            method = "checkStraySpawnRules",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/ServerLevelAccessor;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean rub$canSeeSkyOrZone(ServerLevelAccessor level, BlockPos pos) {
        return level.canSeeSky(pos) || level.getBiome(pos).is(Zone.ICE.biome);
    }
}

// lets strays spawn without open sky in the Ice Caves
