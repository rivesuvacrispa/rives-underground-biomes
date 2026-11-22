package com.rivesuvacrispa.rivesundergroundbiomes;

import com.rivesuvacrispa.rivesundergroundbiomes.entity.Ant;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.Frozen;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.JungleSlime;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.JungleSpider;
import com.rivesuvacrispa.rivesundergroundbiomes.entity.MummySkeleton;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.List;

public final class ModEntities {
    public static final List<SpawnEggItem> SPAWN_EGGS = new ArrayList<>();

    public static EntityType<Ant> ANT;
    public static EntityType<MummySkeleton> MUMMY_SKELETON;
    public static EntityType<Frozen> FROZEN;
    public static EntityType<JungleSlime> JUNGLE_SLIME;
    public static EntityType<JungleSpider> JUNGLE_SPIDER;

    public static void init() {
        // ants only come out of anthills so no spawn restriction
        ANT = register(
                "ant",
                FabricEntityTypeBuilder.createMob()
                        .entityFactory(Ant::new)
                        .spawnGroup(MobCategory.MONSTER)
                        .dimensions(EntityDimensions.fixed(0.4F, 0.3F))
                        .defaultAttributes(Silverfish::createAttributes)
                        .trackRangeChunks(8)
                        .build(),
                0x4A2414,
                0xB0562E);
        MUMMY_SKELETON = register(
                "mummy_skeleton",
                FabricEntityTypeBuilder.createMob()
                        .entityFactory(MummySkeleton::new)
                        .spawnGroup(MobCategory.MONSTER)
                        .dimensions(EntityDimensions.fixed(0.6F, 1.99F))
                        .defaultAttributes(AbstractSkeleton::createAttributes)
                        .spawnRestriction(
                                SpawnPlacements.Type.ON_GROUND,
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                Monster::checkMonsterSpawnRules)
                        .trackRangeChunks(8)
                        .build(),
                0xD9C9A0,
                0x6B5636);
        FROZEN = register(
                "frozen",
                FabricEntityTypeBuilder.createMob()
                        .entityFactory(Frozen::new)
                        .spawnGroup(MobCategory.MONSTER)
                        .dimensions(EntityDimensions.fixed(0.6F, 1.95F))
                        .defaultAttributes(() -> Zombie.createAttributes()
                                .add(Attributes.MAX_HEALTH, 24.0)
                                .add(Attributes.MOVEMENT_SPEED, 0.21))
                        .spawnRestriction(
                                SpawnPlacements.Type.ON_GROUND,
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                Monster::checkMonsterSpawnRules)
                        .trackRangeChunks(8)
                        .build(),
                0x8CC4DE,
                0x2E5A73);
        JUNGLE_SLIME = register(
                "jungle_slime",
                FabricEntityTypeBuilder.createMob()
                        .entityFactory(JungleSlime::new)
                        .spawnGroup(MobCategory.MONSTER)
                        .dimensions(EntityDimensions.scalable(2.04F, 2.04F))
                        .defaultAttributes(Monster::createMonsterAttributes)
                        .spawnRestriction(
                                SpawnPlacements.Type.ON_GROUND,
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                JungleSlime::checkJungleSlimeSpawnRules)
                        .trackRangeChunks(10)
                        .build(),
                0x4F8A2C,
                0x8C6B2E);
        JUNGLE_SPIDER = register(
                "jungle_spider",
                FabricEntityTypeBuilder.createMob()
                        .entityFactory(JungleSpider::new)
                        .spawnGroup(MobCategory.MONSTER)
                        .dimensions(EntityDimensions.fixed(1.4F, 0.9F))
                        .defaultAttributes(Spider::createAttributes)
                        .spawnRestriction(
                                SpawnPlacements.Type.ON_GROUND,
                                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                Monster::checkMonsterSpawnRules)
                        .trackRangeChunks(8)
                        .build(),
                0x2F3D1C,
                0x8BBF3A);
    }

    private static <T extends Mob> EntityType<T> register(String name, EntityType<T> type, int eggColor, int eggSpots) {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, RivesUndergroundBiomes.id(name), type);
        SpawnEggItem egg = Registry.register(
                BuiltInRegistries.ITEM,
                RivesUndergroundBiomes.id(name + "_spawn_egg"),
                new SpawnEggItem(type, eggColor, eggSpots, new Item.Properties()));
        SPAWN_EGGS.add(egg);
        ModBlocks.ALL_ITEMS.add(egg);
        return type;
    }

    private ModEntities() {}
}
