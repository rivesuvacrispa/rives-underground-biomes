package com.rivesuvacrispa.rivesundergroundbiomes.block;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public enum OreKind {
    COAL(
            "coal",
            Blocks.COAL_ORE,
            Blocks.DEEPSLATE_COAL_ORE,
            UniformInt.of(0, 2),
            null,
            BlockTags.COAL_ORES,
            ItemTags.COAL_ORES,
            Items.COAL,
            0.1F),
    IRON(
            "iron",
            Blocks.IRON_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            ConstantInt.of(0),
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.IRON_ORES,
            ItemTags.IRON_ORES,
            Items.IRON_INGOT,
            0.7F),
    COPPER(
            "copper",
            Blocks.COPPER_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            ConstantInt.of(0),
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.COPPER_ORES,
            ItemTags.COPPER_ORES,
            Items.COPPER_INGOT,
            0.7F),
    GOLD(
            "gold",
            Blocks.GOLD_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            ConstantInt.of(0),
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.GOLD_ORES,
            ItemTags.GOLD_ORES,
            Items.GOLD_INGOT,
            1.0F),
    REDSTONE(
            "redstone",
            Blocks.REDSTONE_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            null,
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.REDSTONE_ORES,
            ItemTags.REDSTONE_ORES,
            Items.REDSTONE,
            0.7F),
    LAPIS(
            "lapis",
            Blocks.LAPIS_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            UniformInt.of(2, 5),
            BlockTags.NEEDS_STONE_TOOL,
            BlockTags.LAPIS_ORES,
            ItemTags.LAPIS_ORES,
            Items.LAPIS_LAZULI,
            0.2F),
    DIAMOND(
            "diamond",
            Blocks.DIAMOND_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            UniformInt.of(3, 7),
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.DIAMOND_ORES,
            ItemTags.DIAMOND_ORES,
            Items.DIAMOND,
            1.0F),
    EMERALD(
            "emerald",
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            UniformInt.of(3, 7),
            BlockTags.NEEDS_IRON_TOOL,
            BlockTags.EMERALD_ORES,
            ItemTags.EMERALD_ORES,
            Items.EMERALD,
            1.0F);

    public static final OreKind[] VALUES = values();

    public final String id;
    public final Block stoneOre;
    public final Block deepOre;
    // null for redstone, RedStoneOreBlock has its own xp
    @Nullable
    public final IntProvider xp;

    @Nullable
    public final TagKey<Block> toolTier;

    public final TagKey<Block> blockTag;
    public final TagKey<Item> itemTag;
    public final Item smeltResult;
    public final float smeltXp;

    OreKind(
            String id,
            Block stoneOre,
            Block deepOre,
            @Nullable IntProvider xp,
            @Nullable TagKey<Block> toolTier,
            TagKey<Block> blockTag,
            TagKey<Item> itemTag,
            Item smeltResult,
            float smeltXp) {
        this.id = id;
        this.stoneOre = stoneOre;
        this.deepOre = deepOre;
        this.xp = xp;
        this.toolTier = toolTier;
        this.blockTag = blockTag;
        this.itemTag = itemTag;
        this.smeltResult = smeltResult;
        this.smeltXp = smeltXp;
    }

    public Block vanillaOre(boolean deep) {
        return deep ? deepOre : stoneOre;
    }
}
