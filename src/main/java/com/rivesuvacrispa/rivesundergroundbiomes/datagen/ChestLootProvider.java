package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.AncientRuinFeature;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.ReefTreasure;
import com.rivesuvacrispa.rivesundergroundbiomes.world.feature.SunkenSubmarineFeature;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.function.BiConsumer;

public class ChestLootProvider extends SimpleFabricLootTableProvider {
    public ChestLootProvider(FabricDataOutput output) {
        super(output, LootContextParamSets.CHEST);
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> exporter) {
        exporter.accept(
                ReefTreasure.LOOT,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 6))
                                .add(item(Items.PRISMARINE_SHARD, 2, 8, 20))
                                .add(item(Items.PRISMARINE_CRYSTALS, 1, 6, 15))
                                .add(item(Items.GOLD_INGOT, 1, 4, 12))
                                .add(item(Items.IRON_INGOT, 1, 5, 12))
                                .add(item(Items.LAPIS_LAZULI, 2, 8, 10))
                                .add(item(Items.EMERALD, 1, 3, 8))
                                .add(item(Items.GLOW_INK_SAC, 1, 4, 10))
                                .add(item(Items.SEA_PICKLE, 1, 4, 10))
                                .add(item(Items.NAUTILUS_SHELL, 1, 1, 6))
                                .add(item(Items.WET_SPONGE, 1, 2, 4)))
                        .withPool(LootPool.lootPool()
                                .add(EmptyLootItem.emptyItem().setWeight(20))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(10)
                                        .apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
                                .add(item(Items.DIAMOND, 1, 2, 6))
                                .add(item(Items.GOLDEN_APPLE, 1, 1, 5))
                                .add(item(Items.HEART_OF_THE_SEA, 1, 1, 2))));
        exporter.accept(
                SunkenSubmarineFeature.LOOT,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(5, 9))
                                .add(item(Items.COPPER_INGOT, 4, 12, 20))
                                .add(item(Items.IRON_INGOT, 3, 8, 18))
                                .add(item(Items.GOLD_INGOT, 2, 6, 14))
                                .add(item(Items.REDSTONE, 4, 12, 14))
                                .add(item(Items.EMERALD, 2, 5, 10))
                                .add(item(Items.DIAMOND, 1, 3, 8))
                                .add(item(Items.GUNPOWDER, 2, 6, 10))
                                .add(item(Items.GLOW_INK_SAC, 2, 6, 8))
                                .add(item(Items.PRISMARINE_CRYSTALS, 2, 8, 8))
                                .add(item(Items.NAUTILUS_SHELL, 1, 3, 6))
                                .add(item(Items.SPYGLASS, 1, 1, 4))
                                .add(item(Items.COMPASS, 1, 1, 4))
                                .add(item(Items.CLOCK, 1, 1, 4)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 2))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(12)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                                        UniformGenerator.between(20, 39))
                                                .allowTreasure()))
                                .add(LootItem.lootTableItem(Items.DIAMOND_PICKAXE)
                                        .setWeight(4)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                                UniformGenerator.between(20, 39))))
                                .add(LootItem.lootTableItem(Items.TRIDENT)
                                        .setWeight(4)
                                        .apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.3F, 0.9F))))
                                .add(item(Items.HEART_OF_THE_SEA, 1, 1, 6))
                                .add(item(Items.GOLDEN_APPLE, 1, 2, 6))
                                .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1))
                                .add(item(Items.DIAMOND_BLOCK, 1, 1, 2))));
        exporter.accept(
                AncientRuinFeature.LIBRARY,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(2, 7))
                                .add(item(Items.BOOK, 1, 3, 20))
                                .add(item(Items.PAPER, 2, 6, 20))
                                .add(item(Items.FEATHER, 1, 3, 8))
                                .add(item(Items.INK_SAC, 1, 3, 8))
                                .add(item(Items.CANDLE, 1, 2, 6))
                                .add(item(Items.WRITABLE_BOOK, 1, 1, 3))
                                .add(item(Items.MAP, 1, 1, 2))
                                .add(item(Items.COMPASS, 1, 1, 2))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(8)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                                UniformGenerator.between(10, 25))))));
        exporter.accept(
                AncientRuinFeature.LIBRARY_HIDDEN,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 6))
                                .add(item(Items.GOLD_INGOT, 2, 6, 14))
                                .add(item(Items.EMERALD, 2, 5, 10))
                                .add(item(Items.LAPIS_LAZULI, 4, 10, 10))
                                .add(item(Items.EXPERIENCE_BOTTLE, 2, 6, 10))
                                .add(item(Items.DIAMOND, 1, 3, 6))
                                .add(item(Items.GOLDEN_APPLE, 1, 1, 5))
                                .add(item(Items.NAME_TAG, 1, 1, 4)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 2))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(12)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                                        UniformGenerator.between(30, 39))
                                                .allowTreasure()))
                                .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1))));
        exporter.accept(
                AncientRuinFeature.OBSERVATORY,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 7))
                                .add(item(Items.AMETHYST_SHARD, 2, 6, 16))
                                .add(item(Items.COPPER_INGOT, 2, 8, 14))
                                .add(item(Items.PAPER, 2, 6, 14))
                                .add(item(Items.BOOK, 1, 2, 10))
                                .add(item(Items.CANDLE, 1, 3, 8))
                                .add(item(Items.GLOWSTONE_DUST, 2, 6, 8))
                                .add(item(Items.SPYGLASS, 1, 1, 4))
                                .add(item(Items.CLOCK, 1, 1, 4))
                                .add(item(Items.COMPASS, 1, 1, 4))
                                .add(item(Items.MAP, 1, 1, 4))));
        exporter.accept(
                AncientRuinFeature.OBSERVATORY_HIDDEN,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(3, 6))
                                .add(item(Items.AMETHYST_SHARD, 4, 10, 14))
                                .add(item(Items.GOLD_INGOT, 2, 6, 12))
                                .add(item(Items.EMERALD, 2, 5, 10))
                                .add(item(Items.EXPERIENCE_BOTTLE, 2, 5, 8))
                                .add(item(Items.ENDER_PEARL, 1, 3, 6))
                                .add(item(Items.DIAMOND, 1, 3, 6))
                                .add(item(Items.SPYGLASS, 1, 1, 4)))
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 2))
                                .add(LootItem.lootTableItem(Items.BOOK)
                                        .setWeight(10)
                                        .apply(EnchantWithLevelsFunction.enchantWithLevels(
                                                        UniformGenerator.between(20, 39))
                                                .allowTreasure()))
                                .add(item(Items.GOLDEN_APPLE, 1, 2, 6))
                                .add(item(Items.RECOVERY_COMPASS, 1, 1, 3))
                                .add(item(Items.ENCHANTED_GOLDEN_APPLE, 1, 1, 1))));
    }

    private static LootPoolSingletonContainer.Builder<?> item(ItemLike item, int min, int max, int weight) {
        return LootItem.lootTableItem(item)
                .setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }
}
