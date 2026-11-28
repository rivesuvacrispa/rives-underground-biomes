package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

abstract class TemplateFeature<C extends FeatureConfiguration> extends Feature<C> {
    private static final int ROOF = 15;
    private static final int FLOOR_SCAN = 24;

    TemplateFeature(Codec<C> codec) {
        super(codec);
    }

    static StructureTemplate template(WorldGenLevel level, List<ResourceLocation> ids, RandomSource random) {
        ResourceLocation id = ids.get(random.nextInt(ids.size()));
        return level.getLevel()
                .getServer()
                .getStructureManager()
                .get(id)
                .orElseThrow(() -> new IllegalStateException("Missing structure template " + id));
    }

    static StructurePlaceSettings settings(RandomSource random, StructureProcessor... processors) {
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(Rotation.getRandom(random))
                .setMirror(random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK)
                .setRandom(random);
        for (StructureProcessor processor : processors) settings.addProcessor(processor);
        return settings;
    }

    // where to place the template so it is centered on x z as far as the chunk allows
    // features can write 16 blocks into the neighbors
    // returns null if it is wider than that
    @Nullable
    static BlockPos startInChunk(
            StructureTemplate template,
            StructurePlaceSettings settings,
            ChunkPos chunk,
            int reach, // how far it may stick out of the chunk
            int x,
            int minY,
            int z) {
        BoundingBox box = template.getBoundingBox(settings, BlockPos.ZERO);
        int sizeX = box.getXSpan();
        int sizeZ = box.getZSpan();
        if (sizeX > 16 + 2 * reach || sizeZ > 16 + 2 * reach) return null;
        int minX = Mth.clamp(x - sizeX / 2, chunk.getMinBlockX() - reach, chunk.getMaxBlockX() + 1 + reach - sizeX);
        int minZ = Mth.clamp(z - sizeZ / 2, chunk.getMinBlockZ() - reach, chunk.getMaxBlockZ() + 1 + reach - sizeZ);
        return new BlockPos(minX - box.minX(), minY - box.minY(), minZ - box.minZ());
    }

    static BoundingBox place(
            WorldGenLevel level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos start,
            RandomSource random) {
        template.placeInWorld(level, start, start, settings, random, Block.UPDATE_CLIENTS);
        BoundingBox box = template.getBoundingBox(settings, start);
        // replace accidentally left flowing water with source water
        for (BlockPos pos :
                BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockState state = level.getBlockState(pos);
            if (state.is(Blocks.WATER) && !state.getFluidState().isSource())
                Carving.set(level, pos, Blocks.WATER.defaultBlockState());
        }
        return box;
    }

    static void fillContainers(
            WorldGenLevel level,
            BoundingBox box,
            RandomSource random,
            Function<BlockEntity, ResourceLocation> lootTable) {
        for (BlockPos pos :
                BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof RandomizableContainerBlockEntity) {
                RandomizableContainerBlockEntity.setLootTable(level, random, pos, lootTable.apply(entity));
            }
        }
    }

    // grows a column on through water, up or down, until it meets the floor or the ceiling
    static void extendColumn(WorldGenLevel level, BlockPos end, int step, int max) {
        BlockState column = level.getBlockState(end);
        // an end broken by the ruin decay grows nothing
        if (!column.is(ModBlockTags.PEARLSTONE_MASONRY)) return;
        BlockPos.MutableBlockPos pos = end.mutable().move(0, step, 0);
        for (int i = 0; i < max && isOpen(level.getBlockState(pos)); i++) {
            Carving.set(level, pos, column);
            pos.move(0, step, 0);
        }
    }

    // water above a floor of the reef layers somewhere in the chunk, or null
    @Nullable
    static BlockPos reefFloor(WorldGenLevel level, ChunkAccess chunk, ChunkBiomes biomes, RandomSource random) {
        ChunkPos chunkPos = chunk.getPos();
        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);
        int minY = chunk.getMinBuildHeight() + 6;
        int maxY = chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x & 15, z & 15) - ROOF;
        if (maxY <= minY) return null;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, minY + random.nextInt(maxY - minY), z);
        if (!isOpen(level.getBlockState(pos))) return null;
        for (int i = 0; i < FLOOR_SCAN && pos.getY() > minY; i++) {
            pos.move(0, -1, 0);
            if (!isOpen(level.getBlockState(pos))) {
                BlockPos floor = pos.above();
                return biomes.inBiome(x, floor.getY(), z, Zone.REEF) ? floor : null;
            }
        }
        return null;
    }

    static boolean isOpen(BlockState state) {
        return state.getFluidState().is(FluidTags.WATER) && !state.blocksMotion();
    }

    static boolean isOpen(WorldGenLevel level, int x, int y, int z) {
        return isOpen(level.getBlockState(new BlockPos(x, y, z)));
    }
}
