package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.rivesuvacrispa.rivesundergroundbiomes.world.StoneConverter;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// stone conversion runs after all chunk features so vanilla and modded ores are already placed
@Mixin(ChunkGenerator.class)
public abstract class ChunkGeneratorMixin {
    @Inject(method = "applyBiomeDecoration", at = @At("RETURN"))
    private void rub$convertStone(
            WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager, CallbackInfo ci) {
        StoneConverter.convert(level, chunk);
    }
}
