package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SunkenSubmarineFeature extends TemplateFeature<SunkenSubmarineFeature.Config> {
    public record Config(
            List<ResourceLocation> templates, ResourceLocation chests, ResourceLocation barrels, boolean surface)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(Config::templates),
                        ResourceLocation.CODEC.fieldOf("chests").forGetter(Config::chests),
                        ResourceLocation.CODEC.fieldOf("barrels").forGetter(Config::barrels),
                        Codec.BOOL.fieldOf("surface").forGetter(Config::surface))
                .apply(instance, Config::new));
    }

    public static final ResourceLocation LOOT = RivesUndergroundBiomes.id("chests/lost_submarine");
    private static final int TRIES = 64;
    private static final float EXPOSED = 0.5F;
    private static final float SUPPORTED = 0.5F;
    private static final int MAX_GAP = 2;

    public SunkenSubmarineFeature() {
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
        int height = template.getSize().getY();
        for (int i = 0; i < TRIES; i++) {
            BlockPos floor =
                    config.surface() ? seaFloor(level, chunk, random) : reefFloor(level, chunk, biomes, random);
            if (floor == null) continue;
            StructurePlaceSettings settings = settings(random, BlockIgnoreProcessor.STRUCTURE_AND_AIR);
            int sunk = 1 + random.nextInt(Math.max(1, height - 2));
            BlockPos start =
                    startInChunk(template, settings, chunkPos, 0, floor.getX(), floor.getY() - sunk, floor.getZ());
            if (start == null) return false;
            if (!fits(level, template.getBoundingBox(settings, start))) continue;

            BoundingBox box = place(level, template, settings, start, random);
            fillContainers(
                    level,
                    box,
                    random,
                    entity -> entity instanceof BarrelBlockEntity ? config.barrels() : config.chests());
            return true;
        }
        return false;
    }

    // it may dig into the ground or a wall as long as enough of it shows and it does not hang in the water
    private static boolean fits(WorldGenLevel level, BoundingBox box) {
        int exposed = 0;
        int supported = 0;
        for (int x = box.minX(); x <= box.maxX(); x++) {
            for (int z = box.minZ(); z <= box.maxZ(); z++) {
                if (isOpen(level, x, box.maxY() + 1, z)) exposed++;
                for (int y = box.minY() - MAX_GAP; y <= box.maxY(); y++) {
                    if (!isOpen(level, x, y, z)) {
                        supported++;
                        break;
                    }
                }
            }
        }
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || state.is(ModBlockTags.REEF_STRUCTURE)) return false;
        }
        int columns = box.getXSpan() * box.getZSpan();
        return exposed >= EXPOSED * columns && supported >= SUPPORTED * columns;
    }

    @Nullable
    private static BlockPos seaFloor(WorldGenLevel level, ChunkAccess chunk, RandomSource random) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        BlockPos floor = new BlockPos(x, chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15) + 1, z);
        return isOpen(level.getBlockState(floor)) && Zone.ofSurface(level.getBiome(floor)) == Zone.REEF ? floor : null;
    }
}
