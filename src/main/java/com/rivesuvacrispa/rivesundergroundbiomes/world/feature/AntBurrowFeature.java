package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class AntBurrowFeature extends Feature<NoneFeatureConfiguration> {
    private static final int MIN_DEPTH = 12;
    private static final int MAX_DEPTH = 100;
    private static final double FUNNEL_RADIUS = 5;
    private static final int FUNNEL_DEPTH = 3;
    private static final int MAX_DRIFT = 3;

    public AntBurrowFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        ChunkPos chunk = new ChunkPos(context.origin());
        int cx = chunk.getMiddleBlockX();
        int cz = chunk.getMiddleBlockZ();
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, cx, cz) - 1;
        if (Zone.ofSurface(level.getBiome(new BlockPos(cx, surface, cz))) != Zone.DESERT) return false;
        int cave = Carving.findCave(level, cx, cz, surface - MIN_DEPTH, surface - MAX_DEPTH, Zone.DESERT);
        if (cave == Integer.MIN_VALUE) return false;
        return dig(level, context.random(), cx, cz, cave);
    }

    static boolean dig(WorldGenLevel level, RandomSource random, int x, int z, int bottom) {
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z) - 1;
        if (!level.getFluidState(new BlockPos(x, surface + 1, z)).isEmpty()) return false;
        if (Carving.nearSurfaceStructure(level, x, z, (int) FUNNEL_RADIUS + MAX_DRIFT + 1)) return false;
        if (!straightColumnIsDry(level, x, z, surface, bottom)) return false;

        int start = carveFunnel(level, x, z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, start, z);
        int minY = bottom - MAX_DRIFT * 2;
        while (pos.getY() >= minY) {
            if (!carveTunnel(level, pos, random)) break;
            if (level.getBlockState(pos.below()).isAir()) break;
            if (random.nextFloat() < 0.2F) {
                Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                BlockPos next = pos.relative(side);
                if (Math.abs(next.getX() - x) <= MAX_DRIFT && Math.abs(next.getZ() - z) <= MAX_DRIFT) {
                    pos.set(next);
                    if (!carveTunnel(level, pos, random)) break;
                    if (level.getBlockState(pos.below()).isAir()) break;
                }
            }
            pos.move(Direction.DOWN);
        }
        return true;
    }

    private static boolean straightColumnIsDry(WorldGenLevel level, int x, int z, int surface, int bottom) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = surface; y >= bottom; y--) {
            if (!level.getFluidState(pos.set(x, y, z)).isEmpty()) return false;
        }
        return true;
    }

    private static int carveFunnel(WorldGenLevel level, int x, int z) {
        int reach = (int) FUNNEL_RADIUS;
        int start = Integer.MAX_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz) / FUNNEL_RADIUS;
                if (r > 1) continue;
                int depth = (int) Math.round(FUNNEL_DEPTH * (1 - r * r));
                int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x + dx, z + dz) - 1;
                for (int y = top; y > top - depth; y--) {
                    BlockState state = level.getBlockState(pos.set(x + dx, y, z + dz));
                    if (Carving.canOverwrite(state) && state.getFluidState().isEmpty())
                        Carving.set(level, pos, Blocks.AIR.defaultBlockState());
                }
                if (dx == 0 && dz == 0) start = top - depth;
            }
        }
        return start;
    }

    // returns false if the tunnel ends here at air or something it must not dig through
    private static boolean carveTunnel(WorldGenLevel level, BlockPos pos, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || !state.getFluidState().isEmpty() || !Carving.canOverwrite(state)) return false;
        Carving.set(level, pos, Blocks.CAVE_AIR.defaultBlockState());
        for (Direction direction : Direction.values()) hardenSand(level, pos.relative(direction));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (random.nextFloat() < 0.35F)
                hardenSand(level, pos.relative(direction).relative(direction.getClockWise()));
        }
        return true;
    }

    private static void hardenSand(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.SAND) || state.is(Blocks.DIORITE) || state.is(Blocks.ANDESITE) || state.is(Blocks.GRANITE))
            Carving.set(level, pos, Blocks.SANDSTONE.defaultBlockState());
    }
}
