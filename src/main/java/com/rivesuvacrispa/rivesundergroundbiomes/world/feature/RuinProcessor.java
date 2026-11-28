package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModFeatures;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import org.jetbrains.annotations.Nullable;

// a special structure processor that turns full blocks into decayed stuff
// and turns other blocks like stairs and slabs into water
// so they dont turn into solid blocks
public class RuinProcessor extends StructureProcessor {
    public static final Codec<RuinProcessor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.floatRange(0, 1).fieldOf("decay").forGetter(processor -> processor.decay),
                    Codec.floatRange(0, 1).fieldOf("prismarine").forGetter(processor -> processor.prismarine))
            .apply(instance, RuinProcessor::new));

    private final float decay;
    private final float prismarine;

    public RuinProcessor(float decay, float prismarine) {
        this.decay = decay;
        this.prismarine = prismarine;
    }

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
        RandomSource random = settings.getRandom(current.pos());
        if (state.is(ModBlockTags.PEARLSTONE_MASONRY) && random.nextFloat() < decay) {
            BlockState broken = state.isCollisionShapeFullBlock(level, current.pos())
                    ? decayedBlock(random)
                    : Blocks.WATER.defaultBlockState();
            return new StructureBlockInfo(current.pos(), broken, null);
        }
        if (state.is(Blocks.PRISMARINE) && random.nextFloat() >= prismarine)
            return new StructureBlockInfo(current.pos(), Blocks.WATER.defaultBlockState(), null);
        return current;
    }

    private static BlockState decayedBlock(RandomSource random) {
        return switch (random.nextInt(4)) {
            case 0 -> Blocks.MOSS_BLOCK.defaultBlockState();
            case 1 -> Blocks.MOSSY_COBBLESTONE.defaultBlockState();
            case 2 -> ModBlocks.PEARLSTONE.defaultBlockState();
            default -> Blocks.WATER.defaultBlockState();
        };
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return ModFeatures.RUIN_PROCESSOR;
    }
}
