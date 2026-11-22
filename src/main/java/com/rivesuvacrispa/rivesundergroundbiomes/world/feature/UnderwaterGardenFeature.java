package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.TallSeagrassBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

// vanilla kelp, seagrass and pickle features only work on the ocean floor heightmap
public class UnderwaterGardenFeature extends Feature<UnderwaterGardenFeature.Config> {
    public record Config(int tries, int spread, float seagrass, float tallSeagrass, float kelp, float pickles)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.intRange(1, 128).fieldOf("tries").forGetter(Config::tries),
                        Codec.intRange(0, 12).fieldOf("spread").forGetter(Config::spread),
                        Codec.floatRange(0, 1).fieldOf("seagrass").forGetter(Config::seagrass),
                        Codec.floatRange(0, 1).fieldOf("tall_seagrass").forGetter(Config::tallSeagrass),
                        Codec.floatRange(0, 1).fieldOf("kelp").forGetter(Config::kelp),
                        Codec.floatRange(0, 1).fieldOf("pickles").forGetter(Config::pickles))
                .apply(instance, Config::new));
    }

    public UnderwaterGardenFeature() {
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
                            random.nextInt(5) - 2,
                            random.nextInt(config.spread() * 2 + 1) - config.spread());
            BlockPos floor = floor(level, pos);
            if (floor == null) continue;
            float roll =
                    random.nextFloat() * (config.seagrass() + config.tallSeagrass() + config.kelp() + config.pickles());
            if ((roll -= config.seagrass()) < 0) {
                Carving.set(level, floor, Blocks.SEAGRASS.defaultBlockState());
            } else if ((roll -= config.tallSeagrass()) < 0) {
                if (!isWater(level, floor.above())) continue;
                Carving.set(
                        level,
                        floor,
                        Blocks.TALL_SEAGRASS
                                .defaultBlockState()
                                .setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.LOWER));
                Carving.set(
                        level,
                        floor.above(),
                        Blocks.TALL_SEAGRASS
                                .defaultBlockState()
                                .setValue(TallSeagrassBlock.HALF, DoubleBlockHalf.UPPER));
            } else if ((roll -= config.kelp()) < 0) {
                kelp(level, floor, random);
            } else {
                Carving.set(
                        level,
                        floor,
                        Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4)));
            }
            placed = true;
        }
        return placed;
    }

    private static BlockPos floor(WorldGenLevel level, BlockPos pos) {
        if (!isWater(level, pos)) return null;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int i = 0; i < 6; i++) {
            BlockPos below = cursor.below();
            if (!isWater(level, below)) {
                return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                                && !level.getBlockState(below).is(Blocks.MAGMA_BLOCK)
                        ? cursor.immutable()
                        : null;
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }

    private static void kelp(WorldGenLevel level, BlockPos floor, RandomSource random) {
        int height = 2 + random.nextInt(9);
        int length = 0;
        while (length < height && isWater(level, floor.above(length)) && isWater(level, floor.above(length + 1)))
            length++;
        for (int i = 0; i < length; i++) {
            Carving.set(
                    level,
                    floor.above(i),
                    i == length - 1
                            ? Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, random.nextInt(4) + 20)
                            : Blocks.KELP_PLANT.defaultBlockState());
        }
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER)
                && level.getFluidState(pos).isSource();
    }
}
