package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

import java.util.Arrays;

public class PrismarineGrottoFeature extends Feature<NoneFeatureConfiguration> {
    private static final double HALF_LENGTH = 15;
    private static final double HALF_HEIGHT = 10;
    private static final double HALF_WIDTH = 11;
    private static final double EGG = 0.15;
    private static final int REACH = (int) HALF_LENGTH + 3;
    private static final int TOP = (int) Math.ceil(HALF_HEIGHT * (1 + EGG)) + 1;
    private static final double GEODE_CORE = 1.5;
    private static final double GEODE_GAP = 2;
    private static final double GEODE_RADIUS = GEODE_CORE + GEODE_GAP + PrismarineGeode.SHELL;
    private static final long SALT = 404;
    private static final float BURROW_CHANCE = 0.5F;

    public PrismarineGrottoFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        ChunkPos chunk = new ChunkPos(context.origin());
        BlockPos center = new BlockPos(chunk.getMiddleBlockX(), context.origin().getY(), chunk.getMiddleBlockZ());
        if (center.getY() - HALF_HEIGHT - 2 < level.getMinBuildHeight() + 5) return false;
        if (!deepEnough(level, center)) return false;

        double angle = random.nextDouble() * Math.PI;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        ImprovedNoise noise = SeededNoise.get(level.getSeed(), SALT);
        int size = REACH * 2 + 1;
        int[] floors = new int[size * size];
        int[] ceilings = new int[size * size];
        Arrays.fill(floors, Integer.MAX_VALUE);
        Arrays.fill(ceilings, Integer.MIN_VALUE);

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                double u = dx * cos + dz * sin;
                double w = dz * cos - dx * sin;
                double k = 1 + EGG * u / HALF_LENGTH;
                int column = (dx + REACH) * size + dz + REACH;
                for (int dy = -TOP; dy <= TOP; dy++) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    double d = square(u / HALF_LENGTH) + square(dy / (HALF_HEIGHT * k)) + square(w / (HALF_WIDTH * k));
                    d += 0.12 * noise.noise(pos.getX() * 0.12, pos.getY() * 0.12, pos.getZ() * 0.12);
                    boolean underwater = dy <= 0;
                    BlockState state = level.getBlockState(pos);
                    if (d <= 1) {
                        if (!Carving.canOverwrite(state)) continue;
                        if (underwater) Carving.setWater(level, pos);
                        else Carving.set(level, pos, Blocks.CAVE_AIR.defaultBlockState());
                        floors[column] = Math.min(floors[column], pos.getY());
                        ceilings[column] = Math.max(ceilings[column], pos.getY());
                    } else if (underwater
                            && d <= 1.3
                            && (state.isAir() || !state.getFluidState().isEmpty())
                            && Carving.canOverwrite(state)) {
                        // seals caves that cross the walls below the water line
                        Carving.set(level, pos, Blocks.SANDSTONE.defaultBlockState());
                    }
                }
            }
        }

        int centerFloor = floors[REACH * size + REACH];
        if (centerFloor == Integer.MAX_VALUE) return true;
        BlockPos geode = new BlockPos(center.getX(), centerFloor + 2, center.getZ());
        placeGeode(level, geode, random);
        int centerCeiling = ceilings[REACH * size + REACH];
        if (centerCeiling > center.getY() && random.nextFloat() < BURROW_CHANCE)
            AntBurrowFeature.dig(level, random, center.getX(), center.getZ(), centerCeiling);

        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                int column = (dx + REACH) * size + dz + REACH;
                if (floors[column] <= center.getY() && dx * dx + dz * dz > square(GEODE_RADIUS + 1.5)) {
                    decorateFloor(
                            level,
                            pos.set(center.getX() + dx, floors[column], center.getZ() + dz),
                            center.getY() - floors[column],
                            random);
                }
                if (ceilings[column] > center.getY()) {
                    decorateCeiling(
                            level, noise, pos.set(center.getX() + dx, ceilings[column], center.getZ() + dz), random);
                }
            }
        }
        return true;
    }

    private static boolean deepEnough(WorldGenLevel level, BlockPos center) {
        int top = center.getY() + TOP + 6;
        for (int dx = -REACH; dx <= REACH; dx += REACH) {
            for (int dz = -REACH; dz <= REACH; dz += REACH) {
                if (level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, center.getX() + dx, center.getZ() + dz) <= top)
                    return false;
            }
        }
        return true;
    }

    private static void placeGeode(WorldGenLevel level, BlockPos geode, RandomSource random) {
        PrismarineGeode.place(level, geode, GEODE_CORE, GEODE_GAP, false, random);

        double angle = random.nextDouble() * Math.PI * 2;
        double ux = Math.cos(angle) * 0.9;
        double uz = Math.sin(angle) * 0.9;
        double uy = 0.45;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (double t = GEODE_CORE + GEODE_GAP; t <= GEODE_RADIUS + 1; t += 0.5) {
            double px = geode.getX() + 0.5 + ux * t;
            double py = geode.getY() + 0.5 + uy * t;
            double pz = geode.getZ() + 0.5 + uz * t;
            for (int x = (int) Math.floor(px) - 2; x <= (int) Math.floor(px) + 2; x++) {
                for (int y = (int) Math.floor(py) - 2; y <= (int) Math.floor(py) + 2; y++) {
                    for (int z = (int) Math.floor(pz) - 2; z <= (int) Math.floor(pz) + 2; z++) {
                        if (square(x + 0.5 - px) + square(y + 0.5 - py) + square(z + 0.5 - pz) > square(1.6)) continue;
                        double distance = geode.distToCenterSqr(x + 0.5, y + 0.5, z + 0.5);
                        if (distance <= square(GEODE_CORE + 0.5) || distance > square(GEODE_RADIUS + 0.5)) continue;
                        if (Carving.canOverwrite(level.getBlockState(pos.set(x, y, z)))) Carving.setWater(level, pos);
                    }
                }
            }
        }

        Carving.placeTreasureChest(level, geode.below((int) (GEODE_CORE + GEODE_GAP)), random);
    }

    // pos is the lowest water cell of a column
    private static void decorateFloor(WorldGenLevel level, BlockPos pos, int waterAbove, RandomSource random) {
        BlockPos ground = pos.below();
        BlockState state = level.getBlockState(ground);
        if (!state.isSolid() || !Carving.canOverwrite(state)) return;
        float roll = random.nextFloat();
        if (roll < 0.05F) {
            Carving.set(level, ground, Blocks.MAGMA_BLOCK.defaultBlockState());
            return;
        }
        if (roll < 0.08F) {
            Carving.set(level, ground, Blocks.PRISMARINE.defaultBlockState());
        } else if (roll < 0.10F) {
            Carving.set(level, ground, Blocks.DARK_PRISMARINE.defaultBlockState());
        }
        Carving.plantUnderwater(level, pos, waterAbove, random);
    }

    // pos is the highest air cell of a column
    private static void decorateCeiling(WorldGenLevel level, ImprovedNoise noise, BlockPos pos, RandomSource random) {
        BlockPos roof = pos.above();
        BlockState state = level.getBlockState(roof);
        if (!state.isSolid() || !Carving.canOverwrite(state)) return;
        if (noise.noise(pos.getX() * 0.15, 100, pos.getZ() * 0.15) > 0.05) {
            Carving.set(level, roof, Blocks.MOSS_BLOCK.defaultBlockState());
            if (random.nextFloat() < 0.15F) hangCaveVines(level, pos, random);
        } else if (random.nextFloat() < 0.25F) {
            Carving.set(
                    level,
                    pos,
                    Blocks.GLOW_LICHEN
                            .defaultBlockState()
                            .setValue(MultifaceBlock.getFaceProperty(Direction.UP), true));
        }
    }

    private static void hangCaveVines(WorldGenLevel level, BlockPos top, RandomSource random) {
        int length = 1 + random.nextInt(4);
        BlockPos.MutableBlockPos pos = top.mutable();
        for (int i = 0; i < length; i++) {
            boolean last = i == length - 1 || !level.getBlockState(pos.below()).isAir();
            BlockState vine =
                    last ? Blocks.CAVE_VINES.defaultBlockState() : Blocks.CAVE_VINES_PLANT.defaultBlockState();
            Carving.set(level, pos, vine.setValue(CaveVines.BERRIES, random.nextFloat() < 0.3F));
            if (last) return;
            pos.move(Direction.DOWN);
        }
    }

    private static double square(double value) {
        return value * value;
    }
}
