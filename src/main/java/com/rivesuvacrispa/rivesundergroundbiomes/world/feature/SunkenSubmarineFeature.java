package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SunkenSubmarineFeature extends ReefTemplateFeature<SunkenSubmarineFeature.Config> {
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
    private static final int MAX_GAP = 5;

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
        int halfHeight = Math.max(1, template.getSize().getY() / 2);
        for (int i = 0; i < TRIES; i++) {
            BlockPos floor =
                    config.surface() ? seaFloor(level, chunk, random) : reefFloor(level, chunk, biomes, random);
            if (floor == null) continue;
            StructurePlaceSettings settings = settings(random);
            int sunk = 1 + random.nextInt(halfHeight);
            BlockPos start =
                    startInChunk(template, settings, chunkPos, floor.getX(), floor.getY() - sunk, floor.getZ());
            if (start == null) return false;
            // theres not many places it would fit into this way
            // TODO: check actual in-game placement w/o fit check
            // if (!fits(level, template.getBoundingBox(settings, start), halfHeight)) continue;

            BoundingBox box = place(level, template, settings, start, random);
            fillContainers(level, box, random, config.chests(), config.barrels());
            return true;
        }
        return false;
    }

    //    private static boolean fits(WorldGenLevel level, BoundingBox box, int halfHeight) {
    //        for (int x = box.minX(); x <= box.maxX(); x++) {
    //            for (int z = box.minZ(); z <= box.maxZ(); z++) {
    //                if (!isOpen(level, x, box.maxY() + 1, z)) return false;
    //                int y = box.maxY();
    //                while (y >= box.minY() - MAX_GAP && isOpen(level, x, y, z)) y--;
    //                if (y < box.minY() - MAX_GAP || y >= box.minY() + halfHeight) return false;
    //            }
    //        }
    //        return true;
    //    }

    @Nullable
    private static BlockPos seaFloor(WorldGenLevel level, ChunkAccess chunk, RandomSource random) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        BlockPos floor = new BlockPos(x, chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15) + 1, z);
        return isOpen(level.getBlockState(floor)) && Zone.ofSurface(level.getBiome(floor)) == Zone.REEF ? floor : null;
    }
}
