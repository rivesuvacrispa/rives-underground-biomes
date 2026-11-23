package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

// ordinal = index into the resolved biome arrays
public enum Zone {
    DESERT("underground_desert", "has_underground_desert"),
    JUNGLE("underground_jungle", "has_underground_jungle"),
    ICE("ice_caves", "has_ice_caves");

    public static final Zone[] VALUES = values();

    public final ResourceKey<Biome> biome;
    public final TagKey<Biome> surfaceTag;

    Zone(String biome, String surfaceTag) {
        this.biome = ResourceKey.create(Registries.BIOME, RivesUndergroundBiomes.id(biome));
        this.surfaceTag = TagKey.create(Registries.BIOME, RivesUndergroundBiomes.id(surfaceTag));
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
            if (biome.is(zone.biome)) return zone;
        }
        return ofSurface(biome);
    }
}
