package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import java.util.List;

// if template has a jigsaw block named
// rives_underground_biomes:treasure_pile
// its replaced with treasure pile
public final class ReefTreasure {
    public static final ResourceLocation LOOT = RivesUndergroundBiomes.id("chests/reef_ruins");
    public static final List<EntityType<?>> SPAWNER_MOBS = List.of(EntityType.DROWNED, EntityType.GUARDIAN);

    private static final String MARKER =
            RivesUndergroundBiomes.id("treasure_pile").toString();
    private static final float CHEST_CHANCE = 0.2F;
    private static final float PILED_SPAWNER_CHANCE = 0.8F;

    static void placeAll(
            WorldGenLevel level,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos start,
            List<BlockStateProvider> piles,
            RandomSource random) {
        for (StructureBlockInfo jigsaw : template.filterBlocks(start, settings, Blocks.JIGSAW)) {
            if (jigsaw.nbt() != null && MARKER.equals(jigsaw.nbt().getString("name")))
                place(level, jigsaw.pos(), piles, random);
        }
    }

    private static void place(WorldGenLevel level, BlockPos pos, List<BlockStateProvider> piles, RandomSource random) {
        Carving.set(level, pos, Blocks.WATER.defaultBlockState());
        if (random.nextFloat() < CHEST_CHANCE) {
            spawner(level, pos, random);
            chest(level, pos.above(), random);
            return;
        }
        BlockStateProvider pile = piles.get(random.nextInt(piles.size()));
        if (random.nextFloat() < PILED_SPAWNER_CHANCE) {
            spawner(level, pos, random);
        } else {
            Carving.set(level, pos, pile.getState(random, pos));
        }
        mound(level, pos, pile, random);
    }

    private static void spawner(WorldGenLevel level, BlockPos pos, RandomSource random) {
        Carving.set(level, pos, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner)
            spawner.setEntityId(SPAWNER_MOBS.get(random.nextInt(SPAWNER_MOBS.size())), random);
    }

    private static void chest(WorldGenLevel level, BlockPos pos, RandomSource random) {
        Carving.set(
                level,
                pos,
                ModBlocks.PRISMARINE_CHEST
                        .defaultBlockState()
                        .setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(random))
                        .setValue(ChestBlock.WATERLOGGED, true));
        RandomizableContainerBlockEntity.setLootTable(level, random, pos, LOOT);
    }

    private static void mound(WorldGenLevel level, BlockPos center, BlockStateProvider pile, RandomSource random) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 2; dy++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (!inMound(dx, dy, dz, random)) continue;
                    pos.setWithOffset(center, dx, dy, dz);
                    if (TemplateFeature.isOpen(level.getBlockState(pos)))
                        Carving.set(level, pos, pile.getState(random, pos));
                }
            }
        }
    }

    private static boolean inMound(int dx, int dy, int dz, RandomSource random) {
        int ax = Math.abs(dx);
        int az = Math.abs(dz);
        return switch (dy) {
            case -1 -> ax <= 1 && az <= 1 || ax + az == 2 && (ax == 0 || az == 0) && random.nextBoolean();
            case 0 -> ax <= 1 && az <= 1;
            case 1 -> ax + az <= 1 || ax == 1 && az == 1 && random.nextInt(4) == 0;
            case 2 -> ax + az == 0 && random.nextBoolean();
            default -> false;
        };
    }

    private ReefTreasure() {}
}
