package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class SpiderNestFeature extends Feature<NoneFeatureConfiguration> {
    public SpiderNestFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();
        double rx = 5 + random.nextInt(3);
        double ry = 4 + random.nextInt(2);
        double rz = 5 + random.nextInt(3);
        if (center.getY() - ry < level.getMinBuildHeight() + 5) return false;
        double inner = 1 - 1.5 / ry;

        for (int dx = (int) -rx; dx <= rx; dx++) {
            for (int dy = (int) -ry; dy <= ry; dy++) {
                for (int dz = (int) -rz; dz <= rz; dz++) {
                    double d = Carving.distance(dx, dy, dz, rx, ry, rz, random);
                    if (d > 1) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!Carving.canOverwrite(level.getBlockState(pos))) continue;
                    Carving.set(level, pos, d >= inner ? shell(random) : Blocks.CAVE_AIR.defaultBlockState());
                }
            }
        }

        for (int dx = (int) -rx; dx <= rx; dx++) {
            for (int dy = (int) -ry; dy <= ry; dy++) {
                for (int dz = (int) -rz; dz <= rz; dz++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.getBlockState(pos).is(Blocks.CAVE_AIR)) continue;
                    boolean floor = !level.isEmptyBlock(pos.below());
                    if (floor && random.nextFloat() < 0.05F) {
                        Carving.set(level, pos, Blocks.COBWEB.defaultBlockState());
                    } else if (random.nextFloat() < (touchesWall(level, pos) ? 0.5F : 0.15F)) {
                        Carving.set(level, pos, Blocks.JUNGLE_LEAVES.defaultBlockState());
                    }
                }
            }
        }

        BlockPos floor = center;
        while (floor.getY() > center.getY() - ry
                && (level.getBlockState(floor.below()).is(Blocks.CAVE_AIR)
                        || level.getBlockState(floor.below()).is(Blocks.COBWEB))) {
            floor = floor.below();
        }
        if (Carving.canOverwrite(level.getBlockState(floor))) placeSpawnerAndChest(level, floor, random);
        int openings = 3 + random.nextInt(3);
        for (int i = 0; i < openings; i++) carveOpening(level, center, rx, ry, rz, random);
        return true;
    }

    private static void placeSpawnerAndChest(WorldGenLevel level, BlockPos floor, RandomSource random) {
        Carving.set(level, floor, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(floor) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(ModEntities.JUNGLE_SPIDER, random);
        }
        Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        BlockPos chest = floor.relative(side);
        if (Carving.canOverwrite(level.getBlockState(chest)) && !level.isEmptyBlock(chest.below())) {
            Carving.set(level, chest, ModBlocks.SPIDER_CHEST.defaultBlockState().setValue(ChestBlock.FACING, side));
            RandomizableContainerBlockEntity.setLootTable(level, random, chest, BuiltInLootTables.ABANDONED_MINESHAFT);
        }
    }

    private static void carveOpening(
            WorldGenLevel level, BlockPos center, double rx, double ry, double rz, RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2;
        double uy = -0.4 + random.nextDouble() * 1.1;
        double horizontal = Math.sqrt(1 - uy * uy);
        double ux = Math.cos(angle) * horizontal;
        double uz = Math.sin(angle) * horizontal;
        double wall = 1 / Math.sqrt(ux * ux / (rx * rx) + uy * uy / (ry * ry) + uz * uz / (rz * rz));
        double start = wall - 2;
        double end = wall + 3 + random.nextInt(3);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (double t = start; t <= end; t += 0.5) {
            double radius = 1.5 - (t - start) / (end - start);
            double px = center.getX() + 0.5 + ux * t;
            double py = center.getY() + 0.5 + uy * t;
            double pz = center.getZ() + 0.5 + uz * t;
            int reach = (int) Math.ceil(radius);
            for (int x = (int) Math.floor(px) - reach; x <= (int) Math.floor(px) + reach; x++) {
                for (int y = (int) Math.floor(py) - reach; y <= (int) Math.floor(py) + reach; y++) {
                    for (int z = (int) Math.floor(pz) - reach; z <= (int) Math.floor(pz) + reach; z++) {
                        double ox = x + 0.5 - px;
                        double oy = y + 0.5 - py;
                        double oz = z + 0.5 - pz;
                        if (ox * ox + oy * oy + oz * oz > radius * radius) continue;
                        BlockState state = level.getBlockState(pos.set(x, y, z));
                        if (state.isAir() || state.is(Blocks.COBWEB)) continue;
                        if (!Carving.canOverwrite(state)
                                || !state.getFluidState().isEmpty()) continue;
                        Carving.set(
                                level,
                                pos,
                                random.nextFloat() < 0.3F
                                        ? Blocks.COBWEB.defaultBlockState()
                                        : Blocks.CAVE_AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    private static BlockState shell(RandomSource random) {
        float roll = random.nextFloat();
        if (roll < 0.6F) return Blocks.PACKED_MUD.defaultBlockState();
        if (roll < 0.85F) return Blocks.COARSE_DIRT.defaultBlockState();
        return Blocks.ROOTED_DIRT.defaultBlockState();
    }

    private static boolean touchesWall(WorldGenLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            BlockState state = level.getBlockState(pos.relative(direction));
            if (!state.isAir() && !state.is(Blocks.COBWEB)) return true;
        }
        return false;
    }
}
