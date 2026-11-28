package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class LootProvider extends FabricBlockLootTableProvider {
    public LootProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate() {
        ModBlocks.newHosts().forEach(this::dropSelf);
        ModBlocks.decorations().stream()
                .filter(block -> block != ModBlocks.ANTHILL && block != ModBlocks.FROST_CRYSTAL)
                .forEach(this::dropSelf);
        dropWhenSilkTouch(ModBlocks.THIN_ICE);
        dropSelf(ModBlocks.PEARL_SAND);
        add(ModBlocks.FROST_CRYSTAL, createSingleItemTableWithSilkTouch(ModBlocks.FROST_CRYSTAL, Items.ICE));
        dropSelf(ModBlocks.PERMAFROST_BRICKS);
        ModBlocks.chests().forEach(chest -> add(chest, createNameableBlockEntityTable(chest)));
        dropSelf(ModBlocks.PERMAFROST_BRICK_STAIRS);
        dropSelf(ModBlocks.PERMAFROST_BRICK_WALL);
        add(ModBlocks.PERMAFROST_BRICK_SLAB, createSlabItemTable(ModBlocks.PERMAFROST_BRICK_SLAB));
        dropSelf(ModBlocks.PEARLSTONE_BRICKS);
        dropSelf(ModBlocks.PEARLSTONE_BRICK_STAIRS);
        dropSelf(ModBlocks.PEARLSTONE_BRICK_WALL);
        dropSelf(ModBlocks.CHISELED_PEARLSTONE);
        dropSelf(ModBlocks.PEARLSTONE_PILLAR);
        dropSelf(ModBlocks.SMOOTH_PEARLSTONE);
        dropSelf(ModBlocks.SMOOTH_PEARLSTONE_STAIRS);
        dropSelf(ModBlocks.SMOOTH_PEARLSTONE_WALL);
        add(ModBlocks.SMOOTH_PEARLSTONE_SLAB, createSlabItemTable(ModBlocks.SMOOTH_PEARLSTONE_SLAB));
        add(ModBlocks.PEARLSTONE_BRICK_SLAB, createSlabItemTable(ModBlocks.PEARLSTONE_BRICK_SLAB));
        add(ModBlocks.BUDDING_PRISMARINE, noDrop());
        ModBlocks.prismarineBuds().stream()
                .filter(block -> block != ModBlocks.PRISMARINE_CLUSTER)
                .forEach(this::dropWhenSilkTouch);
        add(ModBlocks.PRISMARINE_CLUSTER, prismarineCluster(ModBlocks.PRISMARINE_CLUSTER));
        add(
                ModBlocks.PRISMARINE_CRYSTAL_ORE,
                createSilkTouchDispatchTable(
                        ModBlocks.PRISMARINE_CRYSTAL_ORE,
                        applyExplosionDecay(
                                ModBlocks.PRISMARINE_CRYSTAL_ORE,
                                LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))
                                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))));
        for (Host host : Host.VALUES) {
            for (OreKind kind : OreKind.VALUES) {
                Block ore = ModBlocks.ore(host, kind);
                add(
                        ore,
                        switch (kind) {
                            case COAL -> createOreDrop(ore, Items.COAL);
                            case IRON -> createOreDrop(ore, Items.RAW_IRON);
                            case COPPER -> createCopperOreDrops(ore);
                            case GOLD -> createOreDrop(ore, Items.RAW_GOLD);
                            case REDSTONE -> createRedstoneOreDrops(ore);
                            case LAPIS -> createLapisOreDrops(ore);
                            case DIAMOND -> createOreDrop(ore, Items.DIAMOND);
                            case EMERALD -> createOreDrop(ore, Items.EMERALD);
                        });
            }
        }
    }

    private LootTable.Builder prismarineCluster(Block cluster) {
        return createSilkTouchDispatchTable(
                cluster,
                LootItem.lootTableItem(Items.PRISMARINE_SHARD)
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4)))
                        .apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                        .otherwise(applyExplosionDecay(
                                cluster,
                                LootItem.lootTableItem(Items.PRISMARINE_SHARD)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(2))))));
    }
}
