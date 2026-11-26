package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.block.BiomeChestBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    public static BlockEntityType<BiomeChestBlockEntity> BIOME_CHEST;

    public static void init() {
        BIOME_CHEST = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                RivesUndergroundBiomes.id("biome_chest"),
                BlockEntityType.Builder.of(
                                BiomeChestBlockEntity::new, ModBlocks.chests().toArray(new Block[0]))
                        .build(null));
    }

    private ModBlockEntities() {}
}
