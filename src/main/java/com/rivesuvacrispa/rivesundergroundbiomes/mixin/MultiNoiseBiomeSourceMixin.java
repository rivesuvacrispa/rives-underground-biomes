package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.rivesuvacrispa.rivesundergroundbiomes.world.BiomeSwap;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;
import java.util.stream.Stream;

// puts our biomes below zone surfaces and into possibleBiomes
// so features, /locate and biome mods see them
@Mixin(MultiNoiseBiomeSource.class)
public abstract class MultiNoiseBiomeSourceMixin {
    @Unique
    private volatile Holder<Biome>[] rub$biomes;

    @Shadow
    private Climate.ParameterList<Holder<Biome>> parameters() {
        throw new AssertionError();
    }

    @Unique
    private Holder<Biome>[] rub$biomes() {
        Holder<Biome>[] biomes = rub$biomes;
        if (biomes == null) {
            biomes = BiomeSwap.resolve(parameters());
            rub$biomes = biomes;
        }
        return biomes;
    }

    @ModifyReturnValue(method = "collectPossibleBiomes", at = @At("RETURN"))
    private Stream<Holder<Biome>> rub$addPossibleBiomes(Stream<Holder<Biome>> original) {
        Holder<Biome>[] biomes = rub$biomes();
        return biomes.length == 0 ? original : Stream.concat(original, Arrays.stream(biomes));
    }

    @WrapOperation(
            method = "getNoiseBiome(IIILnet/minecraft/world/level/biome/Climate$Sampler;)Lnet/minecraft/core/Holder;",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/biome/MultiNoiseBiomeSource;getNoiseBiome(Lnet/minecraft/world/level/biome/Climate$TargetPoint;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> rub$swapUnderground(
            MultiNoiseBiomeSource self,
            Climate.TargetPoint target,
            Operation<Holder<Biome>> original,
            @Local(argsOnly = true, ordinal = 0) int x,
            @Local(argsOnly = true, ordinal = 1) int y,
            @Local(argsOnly = true, ordinal = 2) int z,
            @Local(argsOnly = true) Climate.Sampler sampler) {
        Holder<Biome> natural = original.call(self, target);
        Holder<Biome>[] biomes = rub$biomes();
        return biomes.length == 0 ? natural : BiomeSwap.swap(self, biomes, natural, target, x, y, z, sampler);
    }
}
