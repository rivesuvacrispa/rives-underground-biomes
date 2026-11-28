package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.BiomeChestBlockEntity;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;

public class AncientRuinFeature extends TemplateFeature<AncientRuinFeature.Config> {
    public record Config(
            List<ResourceLocation> templates,
            ResourceLocation chests,
            ResourceLocation hiddenChests,
            boolean buried, // should this be sealed in rock or stand on the cave floor
            int spacingChunks)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(Config::templates),
                        ResourceLocation.CODEC.fieldOf("chests").forGetter(Config::chests),
                        ResourceLocation.CODEC.fieldOf("hidden_chests").forGetter(Config::hiddenChests),
                        Codec.BOOL.fieldOf("buried").forGetter(Config::buried),
                        Codec.intRange(0, 16).fieldOf("spacing").forGetter(Config::spacingChunks))
                .apply(instance, Config::new));
    }

    public static final ResourceLocation LIBRARY = RivesUndergroundBiomes.id("chests/ancient_library");
    public static final ResourceLocation LIBRARY_HIDDEN = RivesUndergroundBiomes.id("chests/ancient_library_hidden");
    public static final ResourceLocation OBSERVATORY = RivesUndergroundBiomes.id("chests/ancient_observatory");
    public static final ResourceLocation OBSERVATORY_HIDDEN =
            RivesUndergroundBiomes.id("chests/ancient_observatory_hidden");

    private static final int TRIES = 48;
    // the trying chunk looks for a spot this far into its neighbors too
    private static final int SEARCH = 8;
    private static final int REACH = 16;
    private static final int DEPTH = 30;
    private static final float FLOOR_SUPPORT = 0.85F;
    private static final float CAVE_AIR = 0.6F;

    public AncientRuinFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        Config config = context.config();
        ChunkPos chunkPos = new ChunkPos(context.origin());
        if (!lowestAround(level.getSeed() ^ config.templates().hashCode(), chunkPos, config.spacingChunks()))
            return false;
        StructureTemplate template = template(level, config.templates(), random);
        int minY = level.getMinBuildHeight() + 8;
        for (int i = 0; i < TRIES; i++) {
            int x = chunkPos.getMinBlockX() - SEARCH + random.nextInt(16 + 2 * SEARCH);
            int z = chunkPos.getMinBlockZ() - SEARCH + random.nextInt(16 + 2 * SEARCH);
            int maxY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - DEPTH;
            if (maxY <= minY) continue;
            int y = config.buried() ? minY + random.nextInt(maxY - minY) : caveFloor(level, x, z, minY, maxY, random);
            if (y == Integer.MIN_VALUE) continue;
            StructurePlaceSettings settings = settings(random, BlockIgnoreProcessor.STRUCTURE_BLOCK);
            BlockPos start = startInChunk(template, settings, chunkPos, REACH, x, y, z);
            if (start == null) return false;
            BoundingBox box = template.getBoundingBox(settings, start);
            if (box.maxY() >= maxY || !inDesert(level, box)) continue;
            if (config.buried() ? !buried(level, box) : !inCave(level, box)) continue;

            place(level, template, settings, start, random);
            fillContainers(
                    level,
                    box,
                    random,
                    entity -> entity instanceof BiomeChestBlockEntity ? config.hiddenChests() : config.chests());
            return true;
        }
        return false;
    }

    // y of a random cave floor block in the column
    private static int caveFloor(WorldGenLevel level, int x, int z, int minY, int maxY, RandomSource random) {
        IntList floors = new IntArrayList();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, maxY, z);
        boolean air = false;
        for (int y = maxY; y >= minY; y--) {
            pos.setY(y);
            if (level.getBlockState(pos).isAir()) {
                air = true;
            } else {
                if (air && isRock(level, pos)) floors.add(y);
                air = false;
            }
        }
        return floors.isEmpty() ? Integer.MIN_VALUE : floors.getInt(random.nextInt(floors.size()));
    }

    private static boolean lowestAround(long seed, ChunkPos chunk, int spacing) {
        double own = roll(seed, chunk.x, chunk.z);
        for (int dx = -spacing; dx <= spacing; dx++) {
            for (int dz = -spacing; dz <= spacing; dz++) {
                if ((dx != 0 || dz != 0) && roll(seed, chunk.x + dx, chunk.z + dz) <= own) return false;
            }
        }
        return true;
    }

    private static double roll(long seed, int chunkX, int chunkZ) {
        return RandomSource.create(seed ^ chunkX * 341873128712L ^ chunkZ * 132897987541L)
                .nextDouble();
    }

    private static boolean inDesert(WorldGenLevel level, BoundingBox box) {
        for (int x : new int[] {box.minX(), box.maxX()}) {
            for (int y : new int[] {box.minY(), box.maxY()}) {
                for (int z : new int[] {box.minZ(), box.maxZ()}) {
                    if (!Zone.DESERT.isBiome(level.getBiome(new BlockPos(x, y, z)))) return false;
                }
            }
        }
        return true;
    }

    // completely sealed in rock blocks
    private static boolean buried(WorldGenLevel level, BoundingBox box) {
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)) {
            if (!isRock(level, pos)) return false;
        }
        return true;
    }

    // stands on the rock with  mostly air above it
    private static boolean inCave(WorldGenLevel level, BoundingBox box) {
        int supported = 0;
        int air = 0;
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY() - 1, box.minZ(), box.maxX(), box.minY() - 1, box.maxZ())) {
            if (isRock(level, pos)) supported++;
        }
        for (BlockPos pos :
                BlockPos.betweenClosed(box.minX(), box.minY() + 1, box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) return false;
            if (state.isAir()) air++;
        }
        int area = box.getXSpan() * box.getZSpan();
        return supported >= FLOOR_SUPPORT * area && air >= CAVE_AIR * area * (box.getYSpan() - 1);
    }

    private static boolean isRock(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isSolidRender(level, pos)
                && !(state.getBlock() instanceof FallingBlock)
                && !state.is(BlockTags.SAND)
                && !state.is(BlockTags.DIRT);
    }
}
