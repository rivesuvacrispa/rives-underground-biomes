package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
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
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.List;

abstract class ReefTemplateFeature<C extends FeatureConfiguration> extends Feature<C> {
    private static final int ROOF = 15;
    private static final int FLOOR_SCAN = 24;

    ReefTemplateFeature(Codec<C> codec) {
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

    static StructurePlaceSettings settings(RandomSource random) {
        return new StructurePlaceSettings()
                .setRotation(Rotation.getRandom(random))
                .setMirror(random.nextBoolean() ? Mirror.NONE : Mirror.FRONT_BACK)
                .setRandom(random)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
    }

    // where to place the template so it is centered on x z as far as the chunk allows
    // null if it is wider than the chunk
    @Nullable
    static BlockPos startInChunk(
            StructureTemplate template, StructurePlaceSettings settings, ChunkPos chunk, int x, int minY, int z) {
        BoundingBox box = template.getBoundingBox(settings, BlockPos.ZERO);
        int sizeX = box.getXSpan();
        int sizeZ = box.getZSpan();
        if (sizeX > 16 || sizeZ > 16) return null;
        int minX = Mth.clamp(x - sizeX / 2, chunk.getMinBlockX(), chunk.getMaxBlockX() + 1 - sizeX);
        int minZ = Mth.clamp(z - sizeZ / 2, chunk.getMinBlockZ(), chunk.getMaxBlockZ() + 1 - sizeZ);
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
            ResourceLocation chests,
            ResourceLocation barrels) {
        for (BlockPos pos :
                BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof RandomizableContainerBlockEntity) {
                RandomizableContainerBlockEntity.setLootTable(
                        level, random, pos, entity instanceof BarrelBlockEntity ? barrels : chests);
            }
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
