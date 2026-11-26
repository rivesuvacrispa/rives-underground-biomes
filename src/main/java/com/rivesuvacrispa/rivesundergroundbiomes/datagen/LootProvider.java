package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

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
        dropWhenSilkTouch(ModBlocks.FROST_CRYSTAL);
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
}
