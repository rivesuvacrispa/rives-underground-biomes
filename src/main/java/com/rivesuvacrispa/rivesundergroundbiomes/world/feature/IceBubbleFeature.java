package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Arrays;

public class IceBubbleFeature extends Feature<NoneFeatureConfiguration> {
    private static final double SHELL = 1.6;
    private static final float TREASURE_CHANCE = 0.25F;

    public IceBubbleFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin();
        int diameter = 10 + random.nextInt(11);
        double radius = diameter / 2.0;
        int reach = (int) Math.ceil(radius);
        if (center.getY() - reach < level.getMinBuildHeight() + 5) return false;
        if (center.getY() + reach + 4 >= level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, center.getX(), center.getZ()))
            return false;

        int size = reach * 2 + 1;
        int[] floors = new int[size * size];
        int[] ceilings = new int[size * size];
        Arrays.fill(floors, Integer.MAX_VALUE);
        Arrays.fill(ceilings, Integer.MIN_VALUE);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int column = (dx + reach) * size + dz + reach;
                for (int dy = -reach; dy <= reach; dy++) {
                    double d = Math.sqrt(dx * dx + dy * dy + dz * dz) + (random.nextDouble() - 0.5) * 0.4;
                    if (d > radius) continue;
                    if (!Carving.canOverwrite(level.getBlockState(pos.setWithOffset(center, dx, dy, dz)))) continue;
                    if (d > radius - SHELL) {
                        Carving.set(level, pos, ModBlocks.THIN_ICE.defaultBlockState());
                    } else {
                        Carving.setWater(level, pos);
                        floors[column] = Math.min(floors[column], pos.getY());
                        ceilings[column] = Math.max(ceilings[column], pos.getY());
                    }
                }
            }
        }

        int centerColumn = reach * size + reach;
        if (random.nextFloat() < TREASURE_CHANCE && floors[centerColumn] != Integer.MAX_VALUE) {
            Carving.placeTreasureChest(level, pos.set(center.getX(), floors[centerColumn], center.getZ()), random);
            floors[centerColumn] = Integer.MAX_VALUE;
        }

        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                int column = (dx + reach) * size + dz + reach;
                if (floors[column] < center.getY()) {
                    decorateFloor(
                            level,
                            pos.set(center.getX() + dx, floors[column], center.getZ() + dz),
                            ceilings[column] - floors[column] - 1,
                            random);
                }
                if (ceilings[column] > center.getY() && random.nextFloat() < 0.12F) {
                    Carving.set(level, pos.set(center.getX() + dx, ceilings[column], center.getZ() + dz), lichen());
                }
            }
        }
        return true;
    }

    private static void decorateFloor(WorldGenLevel level, BlockPos pos, int waterAbove, RandomSource random) {
        float roll = random.nextFloat();
        BlockState ground = null;
        if (roll < 0.06F) ground = Blocks.MAGMA_BLOCK.defaultBlockState();
        else if (roll < 0.09F) ground = Blocks.OBSIDIAN.defaultBlockState();
        else if (roll < 0.14F) ground = Blocks.GRAVEL.defaultBlockState();
        else if (roll < 0.17F) ground = Blocks.CLAY.defaultBlockState();
        if (ground == null) {
            Carving.plantUnderwater(level, pos, waterAbove, random);
            return;
        }
        Carving.set(level, pos, ground);
        if (!ground.is(Blocks.MAGMA_BLOCK) && waterAbove > 0)
            Carving.plantUnderwater(level, pos.above(), waterAbove - 1, random);
    }

    private static BlockState lichen() {
        return Blocks.GLOW_LICHEN
                .defaultBlockState()
                .setValue(MultifaceBlock.getFaceProperty(Direction.UP), true)
                .setValue(BlockStateProperties.WATERLOGGED, true);
    }
}
