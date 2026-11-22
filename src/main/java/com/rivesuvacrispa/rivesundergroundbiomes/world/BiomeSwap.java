package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.mojang.datafixers.util.Pair;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.mixin.HolderReferenceAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.Optional;
import java.util.Set;

public final class BiomeSwap {
    // same depth as vanilla cave biomes, about 25 blocks below the surface
    public static final long START_DEPTH = Climate.quantizeCoord(0.2F);
    // in 4 block cells
    private static final int NEIGHBOUR_CELLS = 6;

    private static final int[][] NEIGHBOURS = {
        {NEIGHBOUR_CELLS, 0}, {-NEIGHBOUR_CELLS, 0}, {0, NEIGHBOUR_CELLS}, {0, -NEIGHBOUR_CELLS}
    };
    private static final Set<ResourceKey<Biome>> OVERWORLD_MARKERS =
            Set.of(Biomes.DESERT, Biomes.JUNGLE, Biomes.SNOWY_PLAINS);

    @SuppressWarnings("unchecked")
    public static final Holder<Biome>[] DISABLED = new Holder[0];

    // index = zone ordinal
    // DISABLED if the source is not overworld like
    // holders come from the registry that owns the list own holders
    // so no global registry access is needed
    @SuppressWarnings("unchecked")
    public static Holder<Biome>[] resolve(Climate.ParameterList<Holder<Biome>> parameters) {
        HolderGetter<Biome> getter = null;
        boolean overworld = false;
        for (Pair<Climate.ParameterPoint, Holder<Biome>> entry : parameters.values()) {
            Holder<Biome> biome = entry.getSecond();
            if (getter == null
                    && biome instanceof Holder.Reference<Biome> reference
                    && ((HolderReferenceAccessor) reference).rub$owner() instanceof HolderGetter<?> owner) {
                getter = (HolderGetter<Biome>) owner;
            }
            if (!overworld)
                overworld = biome.unwrapKey().map(OVERWORLD_MARKERS::contains).orElse(false);
        }
        if (!overworld) return DISABLED;
        if (getter == null) {
            RivesUndergroundBiomes.LOGGER.warn(
                    "Overworld biome source without registry-backed biomes; underground biomes disabled for it");
            return DISABLED;
        }
        Holder<Biome>[] ours = new Holder[Zone.VALUES.length];
        for (Zone zone : Zone.VALUES) {
            Optional<Holder.Reference<Biome>> holder = getter.get(zone.biome);
            if (holder.isEmpty()) {
                RivesUndergroundBiomes.LOGGER.error(
                        "Biome {} is missing; underground biomes disabled", zone.biome.location());
                return DISABLED;
            }
            ours[zone.ordinal()] = holder.get();
        }
        return ours;
    }

    // x y z are quart coordinates as passed to getNoiseBiome
    public static Holder<Biome> swap(
            MultiNoiseBiomeSource source,
            Holder<Biome>[] ours,
            Holder<Biome> natural,
            Climate.TargetPoint target,
            int x,
            int y,
            int z,
            Climate.Sampler sampler) {
        if (target.depth() < START_DEPTH) return natural;
        Zone zone = Zone.ofSurface(natural);
        if (zone == null) {
            boolean inherits = natural.is(ModBiomeTags.INHERITS_NEIGHBOURS);
            if (!inherits && !natural.is(ModBiomeTags.REPLACEABLE_CAVES)) return natural;
            Holder<Biome> surface = inherits ? natural : source.getNoiseBiome(atSurface(target));
            zone = Zone.ofSurface(surface);
            if (zone == null && surface.is(ModBiomeTags.INHERITS_NEIGHBOURS))
                zone = neighbourZone(source, x, y, z, sampler);
            if (zone == null) return natural;
        }
        return ours[zone.ordinal()];
    }

    private static Zone neighbourZone(MultiNoiseBiomeSource source, int x, int y, int z, Climate.Sampler sampler) {
        int[] votes = new int[Zone.VALUES.length];
        for (int[] offset : NEIGHBOURS) {
            Zone zone =
                    Zone.ofSurface(source.getNoiseBiome(atSurface(sampler.sample(x + offset[0], y, z + offset[1]))));
            if (zone != null && ++votes[zone.ordinal()] >= 2) return zone;
        }
        return null;
    }

    private static Climate.TargetPoint atSurface(Climate.TargetPoint target) {
        return new Climate.TargetPoint(
                target.temperature(),
                target.humidity(),
                target.continentalness(),
                target.erosion(),
                0L,
                target.weirdness());
    }

    private BiomeSwap() {}
}
