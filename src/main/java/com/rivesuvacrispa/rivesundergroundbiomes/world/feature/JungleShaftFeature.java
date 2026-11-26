package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
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
        int bottom = Carving.findCave(level, cx, cz, surface - MIN_DEPTH, surface - MAX_DEPTH, Zone.JUNGLE);
        if (bottom == Integer.MIN_VALUE) return false;

        double radius = 2.5 + random.nextDouble() * 2.5;
        ImprovedNoise noise = SeededNoise.get(level.getSeed(), SALT);
        int reach = (int) Math.ceil(radius * 1.3 + FUNNEL_WIDTH + 2);
        // cannot spawn directly or near under village/something else
        if (Carving.nearSurfaceStructure(level, cx, cz, reach)) return false;
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
                    if (!level.getFluidState(pos.set(cx + dx, y, cz + dz)).isEmpty()) return false;
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
                    BlockState state = level.getBlockState(pos.set(cx + dx, y, cz + dz));
                    if (!state.isAir() && Carving.canOverwrite(state) && !isRootNet(state)) {
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

    private static boolean isRootNet(BlockState state) {
        return state.is(Blocks.JUNGLE_WOOD) || state.is(Blocks.HANGING_ROOTS);
    }

    private static boolean inside(
            ImprovedNoise noise, int cx, int cz, int columnTop, double radius, int dx, int y, int dz) {
        double r = radius * (1 + 0.3 * noise.noise(cx * 0.1, y * 0.06, cz * 0.1));
        double bowl = Math.max(0, 1 - (columnTop - y) / (double) FUNNEL);
        r += FUNNEL_WIDTH * bowl * bowl;
        r += 0.6 * noise.noise((cx + dx) * 0.25, y * 0.25, (cz + dz) * 0.25);
        return dx * dx + dz * dz <= r * r;
    }
}
