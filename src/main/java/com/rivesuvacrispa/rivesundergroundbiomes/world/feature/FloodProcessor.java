package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.rivesuvacrispa.rivesundergroundbiomes.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

// for templates built in air but placed underwater
public class FloodProcessor extends StructureProcessor {
    public static final FloodProcessor INSTANCE = new FloodProcessor();
    public static final Codec<FloodProcessor> CODEC = Codec.unit(INSTANCE);

    private FloodProcessor() {}

    @Nullable
    @Override
    public StructureBlockInfo processBlock(
            LevelReader level,
            BlockPos offset,
            BlockPos pos,
            StructureBlockInfo original,
            StructureBlockInfo current,
            StructurePlaceSettings settings) {
        BlockState state = current.state();
        if (state.isAir()) return new StructureBlockInfo(current.pos(), Blocks.WATER.defaultBlockState(), null);
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return new StructureBlockInfo(
                    current.pos(), state.setValue(BlockStateProperties.WATERLOGGED, true), current.nbt());
        }
        return current;
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModFeatures.FLOOD_PROCESSOR;
    }
}
