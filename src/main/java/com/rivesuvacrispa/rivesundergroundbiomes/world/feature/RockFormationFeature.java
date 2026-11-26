package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.rivesuvacrispa.rivesundergroundbiomes.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class RockFormationFeature extends Feature<RockFormationFeature.Config> {
    public record Config(BlockStateProvider rock) implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockStateProvider.CODEC.fieldOf("rock").forGetter(Config::rock))
                .apply(instance, Config::new));
    }

    private static final int MAX_WATER = 16;
    private static final int ROOT = 6;

    public RockFormationFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        if (!isWater(level, origin)) return false;
        int water = 0;
        while (water < MAX_WATER && isWater(level, origin.above(water + 1))) water++;
        if (water < 3) return false;

        boolean down = water < MAX_WATER && random.nextBoolean();
        double radius = 1.5 + random.nextDouble() * 2;
        double height = Math.min(water, 6 + random.nextInt(7));
        double lean = random.nextDouble() * 1.5;
        double angle = random.nextDouble() * Math.PI * 2;
        double leanX = Math.cos(angle) * lean;
        double leanZ = Math.sin(angle) * lean;
        int reach = Mth.ceil(radius + lean) + 1;
        int top = Mth.ceil(height);
        int base = down ? origin.getY() + water : origin.getY();
        int step = down ? -1 : 1;
        if (obstructed(level, origin, reach, base, top, down)) return false;

        boolean placed = false;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (spike(dx, 0, dz, radius, height, leanX, leanZ) > 1) continue;
                pos.set(origin.getX() + dx, base - step, origin.getZ() + dz);
                for (int i = 0; i < ROOT && isWater(level, pos); i++) {
                    Carving.set(level, pos, context.config().rock().getState(random, pos));
                    pos.move(0, -step, 0);
                }
                for (int dy = 0; dy < top; dy++) {
                    if (spike(dx, dy, dz, radius, height, leanX, leanZ) > 1 - random.nextFloat() * 0.15F) continue;
                    pos.set(origin.getX() + dx, base + dy * step, origin.getZ() + dz);
                    if (!isWater(level, pos)) continue;
                    Carving.set(level, pos, context.config().rock().getState(random, pos));
                    placed = true;
                }
            }
        }
        return placed;
    }

    private static double spike(int dx, int dy, int dz, double radius, double height, double leanX, double leanZ) {
        if (dy < 0 || dy >= height) return 2;
        double t = dy / height;
        double r = radius * (1 - t);
        if (r <= 0) return 2;
        return Mth.length(dx - leanX * t, dz - leanZ * t) / r;
    }

    private static boolean obstructed(
            WorldGenLevel level, BlockPos origin, int reach, int base, int top, boolean down) {
        int y0 = down ? base - top : base - ROOT;
        int y1 = down ? base + ROOT : base + top;
        for (BlockPos pos : BlockPos.betweenClosed(
                origin.getX() - reach, y0, origin.getZ() - reach, origin.getX() + reach, y1, origin.getZ() + reach)) {
            BlockState state = level.getBlockState(pos);
            if (state.is(BlockTags.CORAL_BLOCKS) || state.is(ModBlockTags.REEF_STRUCTURE)) return true;
        }
        return false;
    }

    private static boolean isWater(WorldGenLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER)
                && level.getFluidState(pos).isSource();
    }
}
