package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;

// templates are built in air standing on the ground
// with no water or waterlogged blocks
// replaceable area is structure_void
public class ReefTempleFeature extends TemplateFeature<ReefTempleFeature.Config> {
    public record Config(List<ResourceLocation> templates, List<BlockStateProvider> piles, float open)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(Config::templates),
                        ExtraCodecs.nonEmptyList(BlockStateProvider.CODEC.listOf())
                                .fieldOf("piles")
                                .forGetter(Config::piles),
                        Codec.floatRange(0, 1).fieldOf("open").forGetter(Config::open))
                .apply(instance, Config::new));
    }

    private static final int TRIES = 16;
    private static final int REACH = 16;
    private static final int MAX_COLUMN = 24;
    private static final int ROOF = 3;
    private static final float MIN_DECAY = 0.1F;
    private static final float MAX_DECAY = 0.2F;
    private static final float CONDUIT_CHANCE = 0.25F;
    private static final float CONDUIT_LEFT = 0.75F;

    public ReefTempleFeature() {
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
        StructureTemplate template = template(level, config.templates(), random);
        for (int i = 0; i < TRIES; i++) {
            BlockPos floor = reefFloor(level, chunk, biomes, random);
            if (floor == null) continue;
            float decay = Mth.randomBetween(random, MIN_DECAY, MAX_DECAY);
            float conduit = random.nextFloat() < CONDUIT_CHANCE ? CONDUIT_LEFT : 0;
            StructurePlaceSettings settings =
                    settings(random, FloodProcessor.INSTANCE, new RuinProcessor(decay, conduit));
            BlockPos start =
                    startInChunk(template, settings, chunkPos, REACH, floor.getX(), floor.getY(), floor.getZ());
            if (start == null) return false;
            if (!fits(level, template.getBoundingBox(settings, start), config.open())) continue;

            BoundingBox box = place(level, template, settings, start, random);
            for (StructureBlockInfo column : template.filterBlocks(start, settings, ModBlocks.PEARLSTONE_PILLAR)) {
                if (column.pos().getY() == box.minY()) extendColumn(level, column.pos(), -1, MAX_COLUMN);
            }
            ReefTreasure.placeAll(level, template, settings, start, config.piles(), random);
            return true;
        }
        return false;
    }

    private static boolean fits(WorldGenLevel level, BoundingBox box, float open) {
        for (int x : new int[] {box.minX(), box.maxX()}) {
            for (int z : new int[] {box.minZ(), box.maxZ()}) {
                if (box.maxY() + ROOF >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z)) return false;
            }
        }
        int water = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) return false;
            if (!box.isInside(pos)) continue;
            if (state.is(ModBlockTags.REEF_STRUCTURE)) return false;
            if (isOpen(state)) water++;
        }
        return water >= open * box.getXSpan() * box.getYSpan() * box.getZSpan();
    }
}
