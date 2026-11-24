package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

// i love terraria styled jungle
// these shafts connect surface with internals of the jungle
public class JungleShaftFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_DEPTH = 12;
    private static final int MAX_DEPTH = 100;
    private static final int FUNNEL = 6;
    private static final double FUNNEL_WIDTH = 4;
    private static final long SALT = 303;

    public JungleShaftFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunk = new ChunkPos(context.origin());
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx, cz) - 1;
        BlockPos top = new BlockPos(cx, surface, cz);
        if (!level.getFluidState(top.above()).isEmpty()) return false;
        if (Zone.ofSurface(level.getBiome(top)) != Zone.JUNGLE) return false;
        int bottom = findCave(level, cx, cz, surface);
        if (bottom == Integer.MIN_VALUE) return false;

        double radius = 2.5 + random.nextDouble() * 2.5;
        ImprovedNoise noise = SeededNoise.get(level.getSeed(), SALT);
        int reach = (int) Math.ceil(radius * 1.3 + FUNNEL_WIDTH + 2);
        // cannot spawn directly or near under village/something else
        if (nearSurfaceStructure(level, cx, cz, reach)) return false;
        int size = reach * 2 + 1;
        int[] surfaces = new int[size * size];
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                surfaces[(dx + reach) * size + dz + reach] =
                        level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx + dx, cz + dz) - 1;
            }
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int columnTop = surfaces[(dx + reach) * size + dz + reach];
                for (int y = bottom; y <= columnTop + 1; y++) {
                    if (!inside(noise, cx, cz, columnTop, radius + 1, dx, y, dz)) continue;
                    BlockState state = level.getBlockState(pos.set(cx + dx, y, cz + dz));
                    if (!state.getFluidState().isEmpty()) return false;
                    if (!state.isAir() && !Carving.canOverwrite(state)) return false;
                }
            }
        }

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int columnTop = surfaces[(dx + reach) * size + dz + reach];
                int lowestCarved = Integer.MAX_VALUE;
                for (int y = bottom; y <= columnTop; y++) {
                    if (!inside(noise, cx, cz, columnTop, radius, dx, y, dz)) continue;
                    lowestCarved = Math.min(lowestCarved, y);
                    if (!level.getBlockState(pos.set(cx + dx, y, cz + dz)).isAir()) {
                        Carving.set(level, pos, Blocks.AIR.defaultBlockState());
                    }
                }
                if (lowestCarved != Integer.MAX_VALUE && columnTop - lowestCarved < FUNNEL) {
                    pos.set(cx + dx, lowestCarved - 1, cz + dz);
                    if (level.getBlockState(pos).is(Blocks.DIRT)) {
                        Carving.set(level, pos, Blocks.GRASS_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        return true;
    }

    // finds first cave y pos under the point on a surface
    private static int findCave(WorldGenLevel level, int x, int z, int surface) {
        int lowest = Math.max(surface - MAX_DEPTH, level.getMinBuildHeight() + 8);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = surface - MIN_DEPTH; y >= lowest; y--) {
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isAir()
                    || !level.getBlockState(pos.below()).isAir()) continue;
            if (level.getBiome(pos).is(Zone.JUNGLE.biome)) return y;
        }
        return Integer.MIN_VALUE;
    }

    private static boolean inside(
            ImprovedNoise noise, int cx, int cz, int columnTop, double radius, int dx, int y, int dz) {
        double r = radius * (1 + 0.3 * noise.noise(cx * 0.1, y * 0.06, cz * 0.1));
        double bowl = Math.max(0, 1 - (columnTop - y) / (double) FUNNEL);
        r += FUNNEL_WIDTH * bowl * bowl;
        r += 0.6 * noise.noise((cx + dx) * 0.25, y * 0.25, (cz + dz) * 0.25);
        return dx * dx + dz * dz <= r * r;
    }

    private static boolean nearSurfaceStructure(WorldGenLevel level, int cx, int cz, int reach) {
        for (int chunkX = SectionPos.blockToSectionCoord(cx - reach);
                chunkX <= SectionPos.blockToSectionCoord(cx + reach);
                chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(cz - reach);
                    chunkZ <= SectionPos.blockToSectionCoord(cz + reach);
                    chunkZ++) {
                for (Structure structure :
                        level.getChunk(chunkX, chunkZ).getAllReferences().keySet()) {
                    if (structure.step() == GenerationStep.Decoration.SURFACE_STRUCTURES) return true;
                }
            }
        }
        return false;
    }
}
