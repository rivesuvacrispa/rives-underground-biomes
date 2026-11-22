package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class NoiseNetFeature extends Feature<NoiseNetFeature.Config> {
    private static final Codec<Zone> ZONE_CODEC = Codec.STRING.xmap(
            name -> Zone.valueOf(name.toUpperCase(Locale.ROOT)),
            zone -> zone.name().toLowerCase(Locale.ROOT));

    public record Config(
            List<BlockState> states,
            Zone zone,
            float scale,
            float yScale,
            float width,
            int salt,
            Optional<TagKey<Block>> pierce,
            Optional<BlockState> hanging,
            float hangingChance)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockState.CODEC.listOf().fieldOf("states").forGetter(Config::states),
                        ZONE_CODEC.fieldOf("zone").forGetter(Config::zone),
                        Codec.floatRange(0.001F, 1).fieldOf("scale").forGetter(Config::scale),
                        Codec.floatRange(0.1F, 4).fieldOf("y_scale").forGetter(Config::yScale),
                        Codec.floatRange(0, 1).fieldOf("width").forGetter(Config::width),
                        Codec.INT.fieldOf("salt").forGetter(Config::salt),
                        TagKey.codec(Registries.BLOCK).optionalFieldOf("pierce").forGetter(Config::pierce),
                        BlockState.CODEC.optionalFieldOf("hanging").forGetter(Config::hanging),
                        Codec.floatRange(0, 1)
                                .optionalFieldOf("hanging_chance", 0F)
                                .forGetter(Config::hangingChance))
                .apply(instance, Config::new));
    }

    public NoiseNetFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Config config = context.config();
        WorldGenLevel level = context.level();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        ChunkBiomes biomes = new ChunkBiomes(chunk);
        ImprovedNoise a = SeededNoise.get(level.getSeed(), config.salt());
        ImprovedNoise b = SeededNoise.get(level.getSeed(), config.salt() + 7919L);
        ImprovedNoise colour = SeededNoise.get(level.getSeed(), config.salt() + 104729L);
        double s = config.scale();
        double sy = s * config.yScale();
        double w = config.width();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        List<BlockPos> placed = new ArrayList<>();
        for (int x = chunkPos.getMinBlockX(); x <= chunkPos.getMaxBlockX(); x++) {
            for (int z = chunkPos.getMinBlockZ(); z <= chunkPos.getMaxBlockZ(); z++) {
                int top = Math.min(chunk.getMaxBuildHeight() - 1, 120);
                for (int y = chunk.getMinBuildHeight() + 1; y <= top; y++) {
                    if (!biomes.inBiome(x, y, z, config.zone())) continue;
                    pos.set(x, y, z);
                    BlockState state = chunk.getBlockState(pos);
                    if (!state.isAir()
                            && !(config.pierce().isPresent()
                                    && state.is(config.pierce().get()))) continue;
                    // one noise band makes sheets that wall caves off
                    // two bands crossing make tubes
                    if (Math.abs(a.noise(x * s, y * sy, z * s)) >= w) continue;
                    if (Math.abs(b.noise(x * s, y * sy, z * s)) >= w) continue;
                    chunk.setBlockState(pos, pick(config.states(), colour, x, y, z), false);
                    placed.add(pos.immutable());
                }
            }
        }
        if (config.hanging().isPresent()) {
            BlockState hanging = config.hanging().get();
            RandomSource random = context.random();
            for (BlockPos net : placed) {
                BlockPos below = net.below();
                if (random.nextFloat() < config.hangingChance()
                        && level.isEmptyBlock(below)
                        && hanging.canSurvive(level, below)) {
                    Carving.set(level, below, hanging);
                }
            }
        }
        return !placed.isEmpty();
    }

    private static BlockState pick(List<BlockState> states, ImprovedNoise colour, int x, int y, int z) {
        if (states.size() == 1) return states.get(0);
        double c = (colour.noise(x / 30.0, y / 20.0, z / 30.0) + 1) / 2;
        return states.get(Math.max(0, Math.min(states.size() - 1, (int) (c * states.size()))));
    }
}
