package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class ReefCarpetFeature extends Feature<ReefCarpetFeature.Config> {
    public record Config(float seagrass, float tallSeagrass, float kelp, float pickles, float corals)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Codec.floatRange(0, 1).fieldOf("seagrass").forGetter(Config::seagrass),
                        Codec.floatRange(0, 1).fieldOf("tall_seagrass").forGetter(Config::tallSeagrass),
                        Codec.floatRange(0, 1).fieldOf("kelp").forGetter(Config::kelp),
                        Codec.floatRange(0, 1).fieldOf("pickles").forGetter(Config::pickles),
                        Codec.floatRange(0, 1).fieldOf("corals").forGetter(Config::corals))
                .apply(instance, Config::new));
    }

    private static final Block[][] CORALS = {
        {Blocks.TUBE_CORAL_BLOCK, Blocks.TUBE_CORAL, Blocks.TUBE_CORAL_FAN},
        {Blocks.BRAIN_CORAL_BLOCK, Blocks.BRAIN_CORAL, Blocks.BRAIN_CORAL_FAN},
        {Blocks.BUBBLE_CORAL_BLOCK, Blocks.BUBBLE_CORAL, Blocks.BUBBLE_CORAL_FAN},
        {Blocks.FIRE_CORAL_BLOCK, Blocks.FIRE_CORAL, Blocks.FIRE_CORAL_FAN},
        {Blocks.HORN_CORAL_BLOCK, Blocks.HORN_CORAL, Blocks.HORN_CORAL_FAN}
    };

    public ReefCarpetFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkBiomes biomes = new ChunkBiomes(chunk);
        int minY = chunk.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        boolean placed = false;
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                int top = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15);
                BlockState below = chunk.getBlockState(pos.set(x, minY, z));
                for (int y = minY + 1; y < top; y++) {
                    BlockState state = chunk.getBlockState(pos.set(x, y, z));
                    if (isWater(state)
                            && !isWater(below)
                            && below.isFaceSturdy(level, pos.below(), Direction.UP)
                            && !below.is(Blocks.MAGMA_BLOCK)
                            && (!below.is(ModBlockTags.REEF_STRUCTURE) || below.is(ModBlockTags.PEARLSTONE_MASONRY))
                            && biomes.inBiome(x, y, z, Zone.REEF)) {
                        placed |= decorate(level, pos.immutable(), below, random, config);
                    }
                    below = state;
                }
            }
        }
        return placed;
    }

    private static boolean decorate(
            WorldGenLevel level, BlockPos pos, BlockState ground, RandomSource random, Config config) {
        if (ground.is(ModBlockTags.REEF_BARE) || ground.is(BlockTags.CORAL_BLOCKS)) {
            if (random.nextFloat() >= config.corals()) return false;
            Carving.set(level, pos, coral(ground, random));
            return true;
        }
        float roll = random.nextFloat();
        if ((roll -= config.seagrass()) < 0) {
            Carving.set(level, pos, Blocks.SEAGRASS.defaultBlockState());
        } else if ((roll -= config.tallSeagrass()) < 0) {
            if (!UnderwaterGardenFeature.tallSeagrass(level, pos))
                Carving.set(level, pos, Blocks.SEAGRASS.defaultBlockState());
        } else if ((roll -= config.kelp()) < 0) {
            if (!UnderwaterGardenFeature.kelp(level, pos, random))
                Carving.set(level, pos, Blocks.SEAGRASS.defaultBlockState());
        } else if ((roll -= config.pickles()) < 0) {
            Carving.set(
                    level,
                    pos,
                    Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 1 + random.nextInt(4)));
        } else {
            return false;
        }
        return true;
    }

    private static BlockState coral(BlockState ground, RandomSource random) {
        Block[] kind = CORALS[random.nextInt(CORALS.length)];
        for (Block[] corals : CORALS) {
            if (ground.is(corals[0])) kind = corals;
        }
        return kind[1 + random.nextInt(2)].defaultBlockState();
    }

    private static boolean isWater(BlockState state) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }
}
