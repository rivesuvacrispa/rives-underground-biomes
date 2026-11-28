package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class ReefShrineFeature extends TemplateFeature<ReefShrineFeature.Config> {
    public record Config(List<ResourceLocation> frames, List<BlockStateProvider> piles)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("frames").forGetter(Config::frames),
                        ExtraCodecs.nonEmptyList(BlockStateProvider.CODEC.listOf())
                                .fieldOf("piles")
                                .forGetter(Config::piles))
                .apply(instance, Config::new));
    }

    private static final int TRIES = 16;
    // template is intended to be placed this much into the ground
    private static final int BURIED = 4;
    // due to its height the shrine requires at least 7 blocks between floor and ceiling
    private static final int MIN_ROOM = 7;
    // how far the corner columns grow on to reach a lower floor or a higher ceiling
    private static final int MAX_COLUMN = 8;
    private static final float MIN_DECAY = 0.1F;
    private static final float MAX_DECAY = 0.2F;

    public ReefShrineFeature() {
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
        StructureTemplate template = template(level, config.frames(), random);
        for (int i = 0; i < TRIES; i++) {
            BlockPos floor = reefFloor(level, chunk, biomes, random);
            if (floor == null) continue;
            float decay = Mth.randomBetween(random, MIN_DECAY, MAX_DECAY);
            StructurePlaceSettings settings =
                    settings(random, BlockIgnoreProcessor.STRUCTURE_AND_AIR, new RuinProcessor(decay, 1));
            BlockPos start =
                    startInChunk(template, settings, chunkPos, 0, floor.getX(), floor.getY() - BURIED, floor.getZ());
            if (start == null) return false;
            if (!fits(level, template.getBoundingBox(settings, start))) continue;

            BoundingBox box = place(level, template, settings, start, random);
            extendColumns(level, box);
            ReefTreasure.placeAll(level, template, settings, start, config.piles(), random);
            return true;
        }
        return false;
    }

    private static boolean fits(WorldGenLevel level, BoundingBox box) {
        BlockPos center = box.getCenter();
        int roomY = box.minY() + BURIED;
        for (int y = roomY; y < roomY + MIN_ROOM; y++) {
            if (!isOpen(level, center.getX(), y, center.getZ())) return false;
        }
        // if the ground is solid
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (reach(level, x, roomY, z, -1, BURIED + MAX_COLUMN) < 0) return false;
            }
        }
        for (int x : new int[] {box.minX(), box.maxX()}) {
            for (int z : new int[] {box.minZ(), box.maxZ()}) {
                if (reach(level, x, box.maxY() + 1, z, 1, MAX_COLUMN) < 0) return false;
            }
        }
        // coral tube walls and hanging clusters in the room
        // or a temple from the next chunk
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), roomY, box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.CORAL_BLOCKS) || state.is(ModBlockTags.REEF_STRUCTURE)) return false;
        }
        return true;
    }

    private static int reach(WorldGenLevel level, int x, int y, int z, int step, int max) {
        for (int i = 0; i <= max; i++) {
            if (!isOpen(level, x, y + i * step, z)) return i;
        }
        return -1;
    }

    private static void extendColumns(WorldGenLevel level, BoundingBox box) {
        for (int x : new int[] {box.minX(), box.maxX()}) {
            for (int z : new int[] {box.minZ(), box.maxZ()}) {
                extendColumn(level, new BlockPos(x, box.minY(), z), -1, MAX_COLUMN);
                extendColumn(level, new BlockPos(x, box.maxY(), z), 1, MAX_COLUMN);
            }
        }
    }
}
