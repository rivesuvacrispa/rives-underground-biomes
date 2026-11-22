package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ModBiomeTags {
    public static final TagKey<Biome> REPLACEABLE_CAVES = create("replaceable_caves");
    public static final TagKey<Biome> INHERITS_NEIGHBOURS = create("inherits_neighbours");

    private static TagKey<Biome> create(String path) {
        return TagKey.create(Registries.BIOME, RivesUndergroundBiomes.id(path));
    }

    private ModBiomeTags() {}
}
