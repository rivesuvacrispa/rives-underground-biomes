package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.concurrent.ConcurrentHashMap;

public final class SeededNoise {
    private static final ConcurrentHashMap<Long, ImprovedNoise> CACHE = new ConcurrentHashMap<>();

    public static ImprovedNoise get(long worldSeed, long salt) {
        long key = worldSeed * 31 + salt;
        // one world seed per server in practice so the cap is rarely hit
        if (CACHE.size() > 64) CACHE.clear();
        return CACHE.computeIfAbsent(key, k -> new ImprovedNoise(new XoroshiroRandomSource(k)));
    }

    private SeededNoise() {}
}
