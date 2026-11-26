package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.List;
import java.util.Optional;

public class ReefShrineFeature extends ReefTemplateFeature<ReefShrineFeature.Config> {
    public record Config(List<ResourceLocation> frames, Optional<BlockStateProvider> pile)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        ResourceLocation.CODEC.listOf().fieldOf("frames").forGetter(Config::frames),
                        BlockStateProvider.CODEC.optionalFieldOf("pile").forGetter(Config::pile))
                .apply(instance, Config::new));
    }

    public static final ResourceLocation LOOT = RivesUndergroundBiomes.id("chests/reef_shrine");
    private static final int TRIES = 16;
    // template is intended to be placed this much into the ground
    private static final int BURIED = 4;
    // due to its height the shrine requires at least 7 blocks between floor and ceiling
    private static final int MIN_ROOM = 7;
    // how far the corner columns grow on to reach a lower floor or a higher ceiling
    private static final int MAX_COLUMN = 8;
    private static final int SPAWNER_Y = 7;

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
            StructurePlaceSettings settings = settings(random);
            BlockPos start =
                    startInChunk(template, settings, chunkPos, floor.getX(), floor.getY() - BURIED, floor.getZ());
            if (start == null) return false;
            if (!fits(level, template.getBoundingBox(settings, start))) continue;

            BoundingBox box = place(level, template, settings, start, random);
            extendColumns(level, box);
            if (config.pile().isPresent()) {
                pile(level, box, config.pile().get(), random);
            } else {
                fillContainers(level, box, random, LOOT, LOOT);
            }
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
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), roomY, box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            if (level.getBlockState(pos).is(BlockTags.CORAL_BLOCKS)) return false;
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
                extend(level, new BlockPos(x, box.minY(), z), -1);
                extend(level, new BlockPos(x, box.maxY(), z), 1);
            }
        }
    }

    private static void extend(WorldGenLevel level, BlockPos end, int step) {
        BlockState column = level.getBlockState(end);
        BlockPos.MutableBlockPos pos = end.mutable().move(0, step, 0);
        for (int i = 0; i < MAX_COLUMN && isOpen(level.getBlockState(pos)); i++) {
            Carving.set(level, pos, column);
            pos.move(0, step, 0);
        }
    }

    private static void pile(WorldGenLevel level, BoundingBox box, BlockStateProvider pile, RandomSource random) {
        BlockPos spawner = box.getCenter().atY(box.minY() + SPAWNER_Y);
        BlockPos chest = spawner.above();
        Carving.set(level, chest, pile.getState(random, chest));
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (!inPile(dx, dy, dz, random)) continue;
                    pos.setWithOffset(spawner, dx, dy, dz);
                    if (isOpen(level.getBlockState(pos))) Carving.set(level, pos, pile.getState(random, pos));
                }
            }
        }
    }

    private static boolean inPile(int dx, int dy, int dz, RandomSource random) {
        int ax = Math.abs(dx);
        int az = Math.abs(dz);
        return switch (dy) {
            case -1 -> ax <= 1 && az <= 1 || ax + az == 2 && (ax == 0 || az == 0) && random.nextBoolean();
            case 0 -> ax <= 1 && az <= 1;
            case 1 -> ax + az <= 1 || ax == 1 && az == 1 && random.nextInt(4) == 0;
            case 2 -> ax + az == 0 && random.nextBoolean();
            default -> false;
        };
    }
}
