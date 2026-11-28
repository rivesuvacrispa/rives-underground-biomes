package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.List;
import java.util.function.Consumer;

public class RecipeProvider extends FabricRecipeProvider {
    public RecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void buildRecipes(Consumer<FinishedRecipe> exporter) {
        for (Host host : Host.VALUES) {
            for (OreKind kind : OreKind.VALUES) {
                cook(exporter, ModBlocks.ore(host, kind), kind.smeltResult, kind.smeltXp);
            }
        }

        oneToOneConversionRecipe(exporter, Items.SANDSTONE, ModBlocks.SANDSLATE, null, 2);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.DECORATIONS, Items.GLOW_ITEM_FRAME)
                .requires(Items.ITEM_FRAME)
                .requires(ModBlocks.GLOWSHROOM)
                .unlockedBy(getHasName(ModBlocks.GLOWSHROOM), has(ModBlocks.GLOWSHROOM))
                .save(exporter, RivesUndergroundBiomes.id("glow_item_frame_from_glowshroom"));
        oneToOneConversionRecipe(exporter, Items.PACKED_MUD, ModBlocks.MUDSTONE, null, 2);
        oneToOneConversionRecipe(exporter, ModBlocks.PERMAFROST, ModBlocks.GLACISLATE, null, 2);
        oreSmelting(
                exporter,
                List.of(ModBlocks.PERMAFROST),
                RecipeCategory.BUILDING_BLOCKS,
                Items.STONE,
                0.1F,
                200,
                "stone");

        RecipeCategory blocks = RecipeCategory.BUILDING_BLOCKS;
        polished(exporter, blocks, ModBlocks.PERMAFROST_BRICKS, ModBlocks.PERMAFROST);
        stairBuilder(ModBlocks.PERMAFROST_BRICK_STAIRS, Ingredient.of(ModBlocks.PERMAFROST_BRICKS))
                .unlockedBy(getHasName(ModBlocks.PERMAFROST_BRICKS), has(ModBlocks.PERMAFROST_BRICKS))
                .save(exporter);
        slab(exporter, blocks, ModBlocks.PERMAFROST_BRICK_SLAB, ModBlocks.PERMAFROST_BRICKS);
        wall(exporter, RecipeCategory.DECORATIONS, ModBlocks.PERMAFROST_BRICK_WALL, ModBlocks.PERMAFROST_BRICKS);
        stonecutterResultFromBase(exporter, blocks, ModBlocks.PERMAFROST_BRICKS, ModBlocks.PERMAFROST);
        for (ItemLike base : List.of(ModBlocks.PERMAFROST, ModBlocks.PERMAFROST_BRICKS)) {
            stonecutterResultFromBase(exporter, blocks, ModBlocks.PERMAFROST_BRICK_STAIRS, base);
            stonecutterResultFromBase(exporter, blocks, ModBlocks.PERMAFROST_BRICK_SLAB, base, 2);
            stonecutterResultFromBase(exporter, RecipeCategory.DECORATIONS, ModBlocks.PERMAFROST_BRICK_WALL, base);
        }

        polished(exporter, blocks, ModBlocks.PEARLSTONE_BRICKS, ModBlocks.PEARLSTONE);
        stairBuilder(ModBlocks.PEARLSTONE_BRICK_STAIRS, Ingredient.of(ModBlocks.PEARLSTONE_BRICKS))
                .unlockedBy(getHasName(ModBlocks.PEARLSTONE_BRICKS), has(ModBlocks.PEARLSTONE_BRICKS))
                .save(exporter);
        slab(exporter, blocks, ModBlocks.PEARLSTONE_BRICK_SLAB, ModBlocks.PEARLSTONE_BRICKS);
        wall(exporter, RecipeCategory.DECORATIONS, ModBlocks.PEARLSTONE_BRICK_WALL, ModBlocks.PEARLSTONE_BRICKS);
        stonecutterResultFromBase(exporter, blocks, ModBlocks.PEARLSTONE_BRICKS, ModBlocks.PEARLSTONE);
        chiseled(exporter, blocks, ModBlocks.CHISELED_PEARLSTONE, ModBlocks.PEARLSTONE_BRICK_SLAB);
        ShapedRecipeBuilder.shaped(blocks, ModBlocks.PEARLSTONE_PILLAR, 2)
                .define('#', ModBlocks.PEARLSTONE_BRICKS)
                .pattern("#")
                .pattern("#")
                .unlockedBy(getHasName(ModBlocks.PEARLSTONE_BRICKS), has(ModBlocks.PEARLSTONE_BRICKS))
                .save(exporter);
        smeltingResultFromBase(exporter, ModBlocks.SMOOTH_PEARLSTONE, ModBlocks.PEARLSTONE);
        stairBuilder(ModBlocks.SMOOTH_PEARLSTONE_STAIRS, Ingredient.of(ModBlocks.SMOOTH_PEARLSTONE))
                .unlockedBy(getHasName(ModBlocks.SMOOTH_PEARLSTONE), has(ModBlocks.SMOOTH_PEARLSTONE))
                .save(exporter);
        slab(exporter, blocks, ModBlocks.SMOOTH_PEARLSTONE_SLAB, ModBlocks.SMOOTH_PEARLSTONE);
        wall(exporter, RecipeCategory.DECORATIONS, ModBlocks.SMOOTH_PEARLSTONE_WALL, ModBlocks.SMOOTH_PEARLSTONE);
        stonecutterResultFromBase(exporter, blocks, ModBlocks.SMOOTH_PEARLSTONE_STAIRS, ModBlocks.SMOOTH_PEARLSTONE);
        stonecutterResultFromBase(exporter, blocks, ModBlocks.SMOOTH_PEARLSTONE_SLAB, ModBlocks.SMOOTH_PEARLSTONE, 2);
        stonecutterResultFromBase(
                exporter, RecipeCategory.DECORATIONS, ModBlocks.SMOOTH_PEARLSTONE_WALL, ModBlocks.SMOOTH_PEARLSTONE);
        for (ItemLike base : List.of(ModBlocks.PEARLSTONE, ModBlocks.PEARLSTONE_BRICKS)) {
            stonecutterResultFromBase(exporter, blocks, ModBlocks.PEARLSTONE_BRICK_STAIRS, base);
            stonecutterResultFromBase(exporter, blocks, ModBlocks.PEARLSTONE_BRICK_SLAB, base, 2);
            stonecutterResultFromBase(exporter, RecipeCategory.DECORATIONS, ModBlocks.PEARLSTONE_BRICK_WALL, base);
            stonecutterResultFromBase(exporter, blocks, ModBlocks.CHISELED_PEARLSTONE, base);
            stonecutterResultFromBase(exporter, blocks, ModBlocks.PEARLSTONE_PILLAR, base);
        }
    }

    private static void cook(Consumer<FinishedRecipe> exporter, ItemLike input, Item result, float xp) {
        String group = getItemName(result);
        oreSmelting(exporter, List.of(input), RecipeCategory.MISC, result, xp, 200, group);
        oreBlasting(exporter, List.of(input), RecipeCategory.MISC, result, xp, 100, group);
    }
}
