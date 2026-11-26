package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

// ordinal = index into the resolved biome arrays
public enum Zone {
    DESERT("underground_desert", null, "has_underground_desert", 0.2F),
    JUNGLE("underground_jungle", null, "has_underground_jungle", 0.2F),
    ICE("ice_caves", null, "has_ice_caves", 0.2F),
    // 0.2 = about 25 blocks below the surface
    // 0.117 = about 15 blocks where the reef layers start
    REEF("underground_reef", "underground_ocean", "has_underground_reef", 0.117F);

    public static final Zone[] VALUES = values();
    public static final int DEEP_BELOW_Y = 0;

    public final ResourceKey<Biome> biome;

    @Nullable
    public final ResourceKey<Biome> deepBiome;

    public final TagKey<Biome> surfaceTag;
    public final long startDepth;

    Zone(String biome, @Nullable String deepBiome, String surfaceTag, float startDepth) {
        this.biome = key(biome);
        this.deepBiome = deepBiome == null ? null : key(deepBiome);
        this.surfaceTag = TagKey.create(Registries.BIOME, RivesUndergroundBiomes.id(surfaceTag));
        this.startDepth = Climate.quantizeCoord(startDepth);
    }

    private static ResourceKey<Biome> key(String path) {
        return ResourceKey.create(Registries.BIOME, RivesUndergroundBiomes.id(path));
    }

    public boolean isBiome(Holder<Biome> holder) {
        return holder.is(biome) || deepBiome != null && holder.is(deepBiome);
    }

    @Nullable
    public static Zone ofSurface(Holder<Biome> biome) {
        if (biome.is(ModBiomeTags.NO_UNDERGROUND_ZONE)) return null;
        for (Zone zone : VALUES) {
            if (biome.is(zone.surfaceTag)) return zone;
        }
        return null;
    }

    // our biome itself or one of its surface biomes
    @Nullable
    public static Zone ofConverted(Holder<Biome> biome) {
        for (Zone zone : VALUES) {
            if (zone.isBiome(biome)) return zone;
        }
        return ofSurface(biome);
    }
}
