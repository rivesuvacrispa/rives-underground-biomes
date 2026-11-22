package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BuriedRelicFeature extends Feature<NoneFeatureConfiguration> {
    public BuriedRelicFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos center = context.origin().below();
        int relics = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 0, 2))) {
            if (Math.abs(pos.getX() - center.getX()) + Math.abs(pos.getZ() - center.getZ()) > 3) continue;
            if (!level.getBlockState(pos).is(BlockTags.BASE_STONE_OVERWORLD) || level.isEmptyBlock(pos.below()))
                continue;
            boolean isCenter = pos.getX() == center.getX() && pos.getZ() == center.getZ();
            if (relics < 2 && (random.nextFloat() < 0.15F || isCenter && relics == 0)) {
                Carving.set(level, pos, Blocks.SUSPICIOUS_SAND.defaultBlockState());
                if (level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
                    brushable.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, random.nextLong());
                }
                relics++;
            } else {
                Carving.set(level, pos, Blocks.SAND.defaultBlockState());
            }
        }
        return relics > 0;
    }
}
