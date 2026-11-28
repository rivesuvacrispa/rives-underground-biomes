package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ReefGeodeFeature extends Feature<NoneFeatureConfiguration> {
    private static final int TRIES = 8;
    private static final double MIN_CORE = 1.5;
    private static final double MAX_CORE = 3.5;
    private static final double MIN_GAP = 2.5;
    private static final double MAX_GAP = 4;
    private static final int BEDROCK_CLEARANCE = 3;
    // below the sea floor heightmap
    private static final int ROOF = 8;

    public ReefGeodeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkBiomes biomes = new ChunkBiomes(chunk);
        for (int i = 0; i < TRIES; i++) {
            double core = Mth.nextDouble(random, MIN_CORE, MAX_CORE);
            double gap = Mth.nextDouble(random, MIN_GAP, MAX_GAP);
            int radius = Mth.ceil(core + gap + PrismarineGeode.SHELL);
            int x = chunkPos.getMinBlockX() + random.nextInt(16);
            int z = chunkPos.getMinBlockZ() + random.nextInt(16);
            int minY = level.getMinBuildHeight() + BEDROCK_CLEARANCE + radius;
            int maxY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15) - ROOF - radius;
            if (maxY <= minY) continue;
            BlockPos center = new BlockPos(x, minY + random.nextInt(maxY - minY), z);
            // in the rock between two layers
            if (TemplateFeature.isOpen(level.getBlockState(center))
                    || !biomes.inBiome(x, center.getY(), z, Zone.REEF)
                    || !fits(level, center, radius)) continue;
            PrismarineGeode.place(level, center, core, gap, true, random);
            return true;
        }
        return false;
    }

    // no air next to it (the edge of the reef, pockets under the sea floor) and no structure in its way
    private static boolean fits(WorldGenLevel level, BlockPos center, int radius) {
        int reach = radius + 1;
        for (BlockPos pos :
                BlockPos.betweenClosed(center.offset(-reach, -reach, -reach), center.offset(reach, reach, reach))) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(ModBlockTags.REEF_STRUCTURE)) return false;
        }
        return true;
    }
}
