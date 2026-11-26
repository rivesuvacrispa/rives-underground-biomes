package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// between the sea floor and the reef biome the rock still has the reef surface biome
// its underground features like gravel, geodes, dungeons and lakes would break into the first layer
// so only features of the reef biome itself may start there
@Mixin(BiomeFilter.class)
public abstract class BiomeFilterMixin {
    @ModifyReturnValue(method = "shouldPlace", at = @At("RETURN"))
    private boolean rub$keepReefRoof(boolean original, PlacementContext context, RandomSource random, BlockPos pos) {
        if (!original) return false;
        WorldGenLevel level = context.getLevel();
        if (Zone.ofSurface(level.getBiome(pos)) != Zone.REEF) return true;
        if (pos.getY() >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ())) return true;
        PlacedFeature feature = context.topFeature().orElseThrow();
        Holder<Biome> reef = level.registryAccess()
                .registryOrThrow(Registries.BIOME)
                .getHolder(Zone.REEF.biome)
                .orElse(null);
        return reef == null
                || context.generator().getBiomeGenerationSettings(reef).hasFeature(feature);
    }
}
