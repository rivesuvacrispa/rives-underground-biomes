package com.rivesuvacrispa.rivesundergroundbiomes.world.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.List;
import java.util.function.Predicate;

// MonsterRoomFeature but with custom blocks and mobs
public class DungeonFeature extends Feature<DungeonFeature.Config> {
    public record Config(BlockStateProvider wall, BlockStateProvider floor, List<EntityType<?>> mobs)
            implements FeatureConfiguration {
        public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockStateProvider.CODEC.fieldOf("wall").forGetter(Config::wall),
                        BlockStateProvider.CODEC.fieldOf("floor").forGetter(Config::floor),
                        ExtraCodecs.nonEmptyList(BuiltInRegistries.ENTITY_TYPE
                                        .byNameCodec()
                                        .listOf())
                                .fieldOf("mobs")
                                .forGetter(Config::mobs))
                .apply(instance, Config::new));
    }

    private static final BlockState AIR = Blocks.CAVE_AIR.defaultBlockState();

    public DungeonFeature() {
        super(Config.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<Config> context) {
        Predicate<BlockState> replaceable = Feature.isReplaceable(BlockTags.FEATURES_CANNOT_REPLACE);
        Config config = context.config();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        WorldGenLevel level = context.level();
        int rx = random.nextInt(2) + 2;
        int rz = random.nextInt(2) + 2;
        int minX = -rx - 1;
        int maxX = rx + 1;
        int minZ = -rz - 1;
        int maxZ = rz + 1;

        int openings = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = -1; y <= 4; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    boolean solid = level.getBlockState(pos).isSolid();
                    if ((y == -1 || y == 4) && !solid) return false;
                    boolean edge = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (edge && y == 0 && level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) openings++;
                }
            }
        }
        if (openings < 1 || openings > 5) return false;

        for (int x = minX; x <= maxX; x++) {
            for (int y = 3; y >= -1; y--) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos pos = origin.offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    boolean shell = x == minX || x == maxX || z == minZ || z == maxZ || y == -1;
                    if (!shell) {
                        if (!state.is(Blocks.CHEST) && !state.is(Blocks.SPAWNER))
                            safeSetBlock(level, pos, AIR, replaceable);
                    } else if (pos.getY() >= level.getMinBuildHeight()
                            && !level.getBlockState(pos.below()).isSolid()) {
                        level.setBlock(pos, AIR, 2);
                    } else if (state.isSolid() && !state.is(Blocks.CHEST)) {
                        BlockStateProvider provider = y == -1 ? config.floor() : config.wall();
                        safeSetBlock(level, pos, provider.getState(random, pos), replaceable);
                    }
                }
            }
        }

        for (int chest = 0; chest < 2; chest++) {
            for (int attempt = 0; attempt < 3; attempt++) {
                BlockPos pos = new BlockPos(
                        origin.getX() + random.nextInt(rx * 2 + 1) - rx,
                        origin.getY(),
                        origin.getZ() + random.nextInt(rz * 2 + 1) - rz);
                if (!level.isEmptyBlock(pos)) continue;
                int walls = 0;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(pos.relative(direction)).isSolid()) walls++;
                }
                if (walls != 1) continue;
                safeSetBlock(
                        level, pos, StructurePiece.reorient(level, pos, Blocks.CHEST.defaultBlockState()), replaceable);
                RandomizableContainerBlockEntity.setLootTable(level, random, pos, BuiltInLootTables.SIMPLE_DUNGEON);
                break;
            }
        }

        safeSetBlock(level, origin, Blocks.SPAWNER.defaultBlockState(), replaceable);
        if (level.getBlockEntity(origin) instanceof SpawnerBlockEntity spawner) {
            List<EntityType<?>> mobs = config.mobs();
            spawner.setEntityId(mobs.get(random.nextInt(mobs.size())), random);
        }
        return true;
    }
}
