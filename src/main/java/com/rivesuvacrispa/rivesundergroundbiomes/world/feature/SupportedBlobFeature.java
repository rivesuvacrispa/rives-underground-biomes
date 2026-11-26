package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

// like an ore blob but safe for reef
// otherwise sand would fall from ceiling and leave air gaps
public class SupportedBlobFeature extends Feature<SupportedBlobFeature.Config> {
    public record Config(BlockState state, IntProvider radius) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockState.CODEC.fieldOf("state").forGetter(Config::state),
                        IntProvider.CODEC.fieldOf("radius").forGetter(Config::radius))
                .apply(instance, Config::new));
    }

    public SupportedBlobFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        int radius = context.config().radius().sample(random);
        float rx = radius + random.nextFloat();
        float ry = Math.max(1, radius - 1) + random.nextFloat();
        float rz = radius + random.nextFloat();
        int placed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // checks for support before placing
        for (int dy = -(int) ry; dy <= ry; dy++) {
            for (int dx = -(int) rx; dx <= rx; dx++) {
                for (int dz = -(int) rz; dz <= rz; dz++) {
                    float d = dx * dx / (rx * rx) + dy * dy / (ry * ry) + dz * dz / (rz * rz);
                    if (d > 1 - random.nextFloat() * 0.3F) continue;
                    pos.setWithOffset(origin, dx, dy, dz);
                    if (!level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD)) continue;
                    if (FallingBlock.isFree(level.getBlockState(pos.below()))) continue;
                    Carving.set(level, pos, context.config().state());
                    placed++;
                }
            }
        }
        return placed > 0;
    }
}
