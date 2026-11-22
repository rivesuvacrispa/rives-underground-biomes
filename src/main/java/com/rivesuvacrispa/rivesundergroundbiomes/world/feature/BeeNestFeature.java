package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BeeNestFeature extends Feature<NoneFeatureConfiguration> {
    public BeeNestFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        if (!level.isEmptyBlock(pos) || level.isEmptyBlock(pos.above())) return false;
        Carving.placeBeeNest(level, pos, context.random(), 1 + context.random().nextInt(3));
        return true;
    }
}
