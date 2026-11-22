package com.rivesuvacrispa.rivesundergroundbiomes.world;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class StoneConverter {
    // cells of margin around the chunk the jitter can reach
    private static final int PAD = 1;

    private static final byte UNKNOWN = 0;
    private static final byte NONE = 1;
    // in 4 block cells
    // the ocean water body reaches under the coast and would get quicksand or ice walls inside
    private static final int OCEAN_MARGIN = 3;

    public static void convert(WorldGenLevel level, ChunkAccess chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        if (!hasZone(sections)) return;

        ChunkPos pos = chunk.getPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();
        int qx0 = QuartPos.fromBlock(minX) - PAD;
        int qz0 = QuartPos.fromBlock(minZ) - PAD;
        int qy0 = QuartPos.fromBlock(chunk.getMinBuildHeight()) - PAD;
        int sizeXZ = 4 + 2 * PAD;
        int sizeY = QuartPos.fromBlock(chunk.getHeight()) + 2 * PAD;
        // UNKNOWN, NONE or 2 + 2 * zone ordinal, +1 inside the zone biome
        byte[] cells = new byte[sizeXZ * sizeXZ * sizeY];
        // per cell column of this chunk
        // 0 unknown
        // 1 no
        // 2 yes
        byte[] nearOcean = new byte[16];
        int seaLevel = level.getSeaLevel();

        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section.hasOnlyAir() || !section.maybeHas(Conversions::isSource)) continue;
            int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
            section.acquire();
            try {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            BlockState state = section.getBlockState(x, y, z);
                            if (!Conversions.isSource(state)) continue;
                            int bx = minX + x;
                            int by = baseY + y;
                            int bz = minZ + z;
                            byte cell = cellAt(level, cells, qx0, qy0, qz0, sizeXZ, bx, by, bz);
                            if (cell == NONE) continue;
                            Zone zone = Zone.VALUES[(cell - 2) >> 1];
                            BlockState converted = Conversions.convert(zone, (cell & 1) == 1, state);
                            if (converted == null) continue;
                            if (state.is(Blocks.WATER) && isNearOcean(level, nearOcean, minX, minZ, x, z, seaLevel))
                                continue;
                            section.setBlockState(x, y, z, converted, false);
                        }
                    }
                }
            } finally {
                section.release();
            }
        }
    }

    // jitters zone borders so they are not boring cubes
    private static byte cellAt(
            WorldGenLevel level, byte[] cells, int qx0, int qy0, int qz0, int sizeXZ, int bx, int by, int bz) {
        long seed = Mth.getSeed(bx, by, bz);
        int qx = (bx + (int) (seed & 3) - 2) >> 2;
        int qy = (by + (int) (seed >> 2 & 3) - 2) >> 2;
        int qz = (bz + (int) (seed >> 4 & 3) - 2) >> 2;
        int sizeY = cells.length / (sizeXZ * sizeXZ);
        int ix = qx - qx0;
        int iy = qy - qy0;
        int iz = qz - qz0;
        if (ix < 0 || iz < 0 || iy < 0 || ix >= sizeXZ || iz >= sizeXZ || iy >= sizeY)
            return cellOf(level.getNoiseBiome(qx, qy, qz));
        int index = (iy * sizeXZ + iz) * sizeXZ + ix;
        if (cells[index] == UNKNOWN) cells[index] = cellOf(level.getNoiseBiome(qx, qy, qz));
        return cells[index];
    }

    private static boolean isNearOcean(
            WorldGenLevel level, byte[] cache, int minX, int minZ, int x, int z, int seaLevel) {
        int index = (z >> 2) * 4 + (x >> 2);
        if (cache[index] == 0) {
            int qx = QuartPos.fromBlock(minX + x);
            int qz = QuartPos.fromBlock(minZ + z);
            int qy = QuartPos.fromBlock(seaLevel);
            boolean ocean = false;
            for (int dx = -OCEAN_MARGIN; dx <= OCEAN_MARGIN && !ocean; dx++) {
                for (int dz = -OCEAN_MARGIN; dz <= OCEAN_MARGIN && !ocean; dz++) {
                    ocean = level.getNoiseBiome(qx + dx, qy, qz + dz).is(BiomeTags.IS_OCEAN);
                }
            }
            cache[index] = (byte) (ocean ? 2 : 1);
        }
        return cache[index] == 2;
    }

    private static byte cellOf(Holder<Biome> biome) {
        for (Zone zone : Zone.VALUES) {
            if (biome.is(zone.biome)) return (byte) (2 + zone.ordinal() * 2 + 1);
        }
        Zone surface = Zone.ofSurface(biome);
        return surface == null ? NONE : (byte) (2 + surface.ordinal() * 2);
    }

    private static boolean hasZone(LevelChunkSection[] sections) {
        for (LevelChunkSection section : sections) {
            if (section.getBiomes().maybeHas(biome -> Zone.ofConverted(biome) != null)) return true;
        }
        return false;
    }

    private StoneConverter() {}
}
