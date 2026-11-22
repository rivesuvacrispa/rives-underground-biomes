package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

final class ChunkBiomes {
    private final ChunkAccess chunk;
    private final int minQuartY;
    private final byte[] cells;

    ChunkBiomes(ChunkAccess chunk) {
        this.chunk = chunk;
        this.minQuartY = QuartPos.fromBlock(chunk.getMinBuildHeight());
        this.cells = new byte[16 * QuartPos.fromBlock(chunk.getHeight())];
    }

    boolean inBiome(int x, int y, int z, Zone zone) {
        return cell(x, y, z) == (byte) (zone.ordinal() * 2 + 2);
    }

    // 0 = unknown
    // 1 = none
    // 2 + 2 * ordinal = zone biome
    // 3 + 2 * ordinal = under a zone surface biome
    private int cell(int x, int y, int z) {
        int qx = QuartPos.fromBlock(x) & 3;
        int qz = QuartPos.fromBlock(z) & 3;
        int qy = QuartPos.fromBlock(y) - minQuartY;
        int index = (qy * 4 + qz) * 4 + qx;
        if (index < 0 || index >= cells.length) return 1;
        byte cell = cells[index];
        if (cell == 0) {
            cell = classify(chunk.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z)));
            cells[index] = cell;
        }
        return cell;
    }

    private static byte classify(Holder<Biome> biome) {
        for (Zone zone : Zone.VALUES) {
            if (biome.is(zone.biome)) return (byte) (zone.ordinal() * 2 + 2);
        }
        Zone surface = Zone.ofSurface(biome);
        return surface == null ? 1 : (byte) (surface.ordinal() * 2 + 3);
    }
}
