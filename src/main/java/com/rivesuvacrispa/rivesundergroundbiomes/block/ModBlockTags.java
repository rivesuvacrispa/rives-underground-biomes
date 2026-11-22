package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class ModBlockTags {
    public static final TagKey<Block> ANTHILL_PLACEABLE =
            TagKey.create(Registries.BLOCK, RivesUndergroundBiomes.id("anthill_placeable"));
    public static final TagKey<Block> ROOT_PIERCEABLE =
            TagKey.create(Registries.BLOCK, RivesUndergroundBiomes.id("root_pierceable"));

    public static final TagKey<Block> SPELEOTHEMS =
            TagKey.create(Registries.BLOCK, RivesUndergroundBiomes.id("speleothems"));

    private ModBlockTags() {}
}
