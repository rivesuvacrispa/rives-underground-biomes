package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModEntities;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class EntityLootProvider extends SimpleFabricLootTableProvider {
    public EntityLootProvider(FabricDataOutput output) {
        super(output, LootContextParamSets.ENTITY);
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> exporter) {
        exporter.accept(ModEntities.FROZEN.getDefaultLootTable(), withExtra(EntityType.ZOMBIE, Items.ICE));
        exporter.accept(ModEntities.MUMMY_SKELETON.getDefaultLootTable(), withExtra(EntityType.SKELETON, Items.SAND));
    }

    private static LootTable.Builder withExtra(EntityType<?> vanilla, ItemLike extra) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().add(LootTableReference.lootTableReference(vanilla.getDefaultLootTable())))
                .withPool(LootPool.lootPool()
                        .add(LootItem.lootTableItem(extra)
                                .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0, 1))))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.65F, 0.1F)));
    }
}
