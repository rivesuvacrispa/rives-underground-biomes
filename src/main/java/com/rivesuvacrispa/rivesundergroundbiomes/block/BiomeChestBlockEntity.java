package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BiomeChestBlockEntity extends ChestBlockEntity {
    public BiomeChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIOME_CHEST, pos, state);
    }
}
