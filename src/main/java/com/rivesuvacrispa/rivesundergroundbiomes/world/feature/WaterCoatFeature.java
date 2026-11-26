package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

// patches of blocks around caves, only replacing blocks that touch water cuz corals die out when they dont do so
public class WaterCoatFeature extends Feature<WaterCoatFeature.Config> {
    public record Config(BlockStateProvider state, IntProvider radius) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockStateProvider.CODEC.fieldOf("state").forGetter(Config::state),
                        IntProvider.CODEC.fieldOf("radius").forGetter(Config::radius))
                .apply(instance, Config::new));
    }

    public WaterCoatFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        BlockState state = context.config().state().getState(random, origin);
        boolean falls = state.getBlock() instanceof FallingBlock;
        int radius = context.config().radius().sample(random);
        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx * dx + dy * dy + dz * dz > radius * radius || random.nextFloat() > 0.75F) continue;
                    pos.setWithOffset(origin, dx, dy, dz);
                    BlockState current = level.getBlockState(pos);
                    if (!current.is(BlockTags.BASE_STONE_OVERWORLD) && !current.is(BlockTags.SAND)) continue;
                    if (!touchesWater(level, pos)) continue;
                    if (falls && FallingBlock.isFree(level.getBlockState(pos.below()))) continue;
                    Carving.set(level, pos, state);
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static boolean touchesWater(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) return true;
        }
        return false;
    }
}
