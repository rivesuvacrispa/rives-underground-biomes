package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class AttachedBlockFeature extends Feature<AttachedBlockFeature.Config> {
    public record Config(BlockState state, int tries, int spread) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockState.CODEC.fieldOf("state").forGetter(Config::state),
                        Codec.intRange(1, 128).fieldOf("tries").forGetter(Config::tries),
                        Codec.intRange(0, 12).fieldOf("spread").forGetter(Config::spread))
                .apply(instance, Config::new));
    }

    public AttachedBlockFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean placed = false;
        for (int i = 0; i < config.tries(); i++) {
            BlockPos pos = context.origin()
                    .offset(
                            random.nextInt(config.spread() * 2 + 1) - config.spread(),
                            random.nextInt(config.spread() + 1) - config.spread() / 2,
                            random.nextInt(config.spread() * 2 + 1) - config.spread());
            if (!level.isEmptyBlock(pos)) continue;
            for (Direction toSupport : Direction.allShuffled(random)) {
                BlockPos supportPos = pos.relative(toSupport);
                if (level.getBlockState(supportPos).isFaceSturdy(level, supportPos, toSupport.getOpposite())) {
                    level.setBlock(
                            pos, config.state().setValue(BlockStateProperties.FACING, toSupport.getOpposite()), 2);
                    placed = true;
                    break;
                }
            }
        }
        return placed;
    }
}
