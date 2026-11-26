package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.mojang.datafixers.util.Pair;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.mixin.HolderReferenceAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public final class BiomeSwap {
    private static final long MIN_START_DEPTH =
            Arrays.stream(Zone.VALUES).mapToLong(zone -> zone.startDepth).min().orElseThrow();
    private static final int NEIGHBOUR_CELLS = 6;

    private static final int[][] NEIGHBOURS = {
        {NEIGHBOUR_CELLS, 0}, {-NEIGHBOUR_CELLS, 0}, {0, NEIGHBOUR_CELLS}, {0, -NEIGHBOUR_CELLS}
    };
    private static final boolean TERRABLENDER = FabricLoader.getInstance().isModLoaded("terrablender");
    private static final Set<ResourceKey<Biome>> OVERWORLD_MARKERS =
            Set.of(Biomes.DESERT, Biomes.JUNGLE, Biomes.SNOWY_PLAINS);

    @SuppressWarnings("unchecked")
    public static final Holder<Biome>[] DISABLED = new Holder[0];

    // index = 2 * zone ordinal, +1 for the deep biome
    // zones without one have their zone biome there again
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
        Holder<Biome>[] ours = new Holder[Zone.VALUES.length * 2];
        for (Zone zone : Zone.VALUES) {
            Holder<Biome> biome = holder(getter, zone.biome);
            Holder<Biome> deep = zone.deepBiome == null ? biome : holder(getter, zone.deepBiome);
            if (biome == null || deep == null) return DISABLED;
            ours[zone.ordinal() * 2] = biome;
            ours[zone.ordinal() * 2 + 1] = deep;
        }
        return ours;
    }

    @Nullable
    private static Holder<Biome> holder(HolderGetter<Biome> getter, ResourceKey<Biome> key) {
        Optional<Holder.Reference<Biome>> holder = getter.get(key);
        if (holder.isEmpty()) {
            RivesUndergroundBiomes.LOGGER.error("Biome {} is missing; underground biomes disabled", key.location());
            return null;
        }
        return holder.get();
    }

    // x y z are quart coordinates as passed to getNoiseBiome
    // natural is the biome the source picked
    // by vanilla or by a mod that replaced the lookup
    public static Holder<Biome> swap(
            Climate.ParameterList<Holder<Biome>> parameters,
            Holder<Biome>[] ours,
            Holder<Biome> natural,
            int x,
            int y,
            int z,
            Climate.Sampler sampler) {
        Zone zone = Zone.ofSurface(natural);
        boolean inherits = false;
        if (zone == null) {
            inherits = natural.is(ModBiomeTags.INHERITS_NEIGHBOURS);
            if (!inherits && !natural.is(ModBiomeTags.REPLACEABLE_CAVES)) return natural;
        }
        // sampled only for candidates since a mod like TerraBlender may have replaced the lookup
        // that held the vanilla target point
        Climate.TargetPoint target = sampler.sample(x, y, z);
        if (target.depth() < MIN_START_DEPTH) return natural;
        if (zone == null) {
            Holder<Biome> surface = inherits ? natural : lookup(parameters, atSurface(target), x, y, z);
            zone = Zone.ofSurface(surface);
            if (zone == null && surface.is(ModBiomeTags.INHERITS_NEIGHBOURS))
                zone = neighbourZone(parameters, x, y, z, sampler);
            if (zone == null) return natural;
        }
        if (target.depth() < zone.startDepth) return natural;
        boolean deep = QuartPos.toBlock(y) < Zone.DEEP_BELOW_Y;
        return ours[zone.ordinal() * 2 + (deep ? 1 : 0)];
    }

    private static Zone neighbourZone(
            Climate.ParameterList<Holder<Biome>> parameters, int x, int y, int z, Climate.Sampler sampler) {
        int[] votes = new int[Zone.VALUES.length];
        for (int[] offset : NEIGHBOURS) {
            int nx = x + offset[0];
            int nz = z + offset[1];
            Zone zone = Zone.ofSurface(lookup(parameters, atSurface(sampler.sample(nx, y, nz)), nx, y, nz));
            if (zone != null && ++votes[zone.ordinal()] >= 2) return zone;
        }
        return null;
    }

    // TerraBlender picks the biome per region
    // so a plain param search miss its surface biomes
    private static Holder<Biome> lookup(
            Climate.ParameterList<Holder<Biome>> parameters, Climate.TargetPoint target, int x, int y, int z) {
        return TERRABLENDER ? TerraBlenderBiomes.findValue(parameters, target, x, y, z) : parameters.findValue(target);
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
