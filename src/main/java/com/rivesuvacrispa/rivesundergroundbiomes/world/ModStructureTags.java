package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

public final class ModStructureTags {
    // reefs wraps around these
    // example: stronghold wins over the reef
    public static final TagKey<Structure> REEF_AVOIDS =
            TagKey.create(Registries.STRUCTURE, RivesUndergroundBiomes.id("reef_avoids"));
    // pieces skipped inside the reef like mineshafts
    public static final TagKey<Structure> NOT_IN_REEF =
            TagKey.create(Registries.STRUCTURE, RivesUndergroundBiomes.id("not_in_reef"));

    private ModStructureTags() {}
}
