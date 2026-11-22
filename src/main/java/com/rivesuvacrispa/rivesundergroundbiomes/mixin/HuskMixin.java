package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// lets husks spawn without open sky in the Underground Desert
@Mixin(Husk.class)
public abstract class HuskMixin {
    @Redirect(
            method = "checkHuskSpawnRules",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/ServerLevelAccessor;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    private static boolean rub$canSeeSkyOrZone(ServerLevelAccessor level, BlockPos pos) {
        return level.canSeeSky(pos) || level.getBiome(pos).is(Zone.DESERT.biome);
    }
}
