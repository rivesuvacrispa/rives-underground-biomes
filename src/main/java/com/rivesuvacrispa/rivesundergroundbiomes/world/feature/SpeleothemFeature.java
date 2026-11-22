package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.block.SpeleothemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

import java.util.Optional;

public class SpeleothemFeature extends Feature<SpeleothemFeature.Config> {
    private static final int MAX_SCAN = 12;

    // onWood = block placed under logs instead of a stalactite
    public record Config(
            Block block,
            int tries,
            int spread,
            IntProvider length,
            float stalactiteChance,
            float stalagmiteChance,
            Optional<BlockState> onWood)
            implements FeatureConfiguration {
        public Config(
                Block block,
                int tries,
                int spread,
                IntProvider length,
                float stalactiteChance,
                float stalagmiteChance) {
            this(block, tries, spread, length, stalactiteChance, stalagmiteChance, Optional.empty());
        }

        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(Config::block),
                        Codec.intRange(1, 128).fieldOf("tries").forGetter(Config::tries),
                        Codec.intRange(0, 12).fieldOf("spread").forGetter(Config::spread),
                        IntProvider.codec(1, 16).fieldOf("length").forGetter(Config::length),
                        Codec.floatRange(0, 1).fieldOf("stalactite_chance").forGetter(Config::stalactiteChance),
                        Codec.floatRange(0, 1).fieldOf("stalagmite_chance").forGetter(Config::stalagmiteChance),
                        BlockState.CODEC.optionalFieldOf("on_wood").forGetter(Config::onWood))
                .apply(instance, Config::new));
    }

    public SpeleothemFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        if (!(config.block() instanceof SpeleothemBlock block)) return false;
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        boolean placed = false;
        for (int i = 0; i < config.tries(); i++) {
            BlockPos pos = context.origin()
                    .offset(
                            random.nextInt(config.spread() * 2 + 1) - config.spread(),
                            random.nextInt(9) - 4,
                            random.nextInt(config.spread() * 2 + 1) - config.spread());
            if (!level.isEmptyBlock(pos)) continue;
            float roll = random.nextFloat();
            Direction tip;
            if (roll < config.stalactiteChance()) tip = Direction.DOWN;
            else if (roll < config.stalactiteChance() + config.stalagmiteChance()) tip = Direction.UP;
            else continue;
            BlockPos start = findStart(level, pos, tip);
            if (start == null) continue;
            if (tip == Direction.DOWN
                    && config.onWood().isPresent()
                    && level.getBlockState(start.above()).is(BlockTags.LOGS)) {
                BlockState wood = config.onWood().get();
                if (wood.canSurvive(level, start)) {
                    level.setBlock(start, wood, 2);
                    placed = true;
                }
                continue;
            }
            placed |= grow(level, block, start, tip, config.length().sample(random));
        }
        return placed;
    }

    private static BlockPos findStart(WorldGenLevel level, BlockPos pos, Direction tip) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        Direction toSupport = tip.getOpposite();
        for (int i = 0; i < MAX_SCAN; i++) {
            BlockPos next = cursor.relative(toSupport);
            if (!level.isEmptyBlock(next)) {
                return level.getBlockState(next).isFaceSturdy(level, next, tip) ? cursor.immutable() : null;
            }
            cursor.move(toSupport);
        }
        return null;
    }

    private static boolean grow(WorldGenLevel level, SpeleothemBlock block, BlockPos start, Direction tip, int wanted) {
        int length = 0;
        while (length < wanted && level.isEmptyBlock(start.relative(tip, length))) length++;
        // leave one air cell before the opposite surface so tips do not touch it
        if (length < wanted && length > 1) length--;
        for (int i = 0; i < length; i++) {
            level.setBlock(start.relative(tip, i), block.columnState(tip, i, length), 2);
        }
        return length > 0;
    }
}
