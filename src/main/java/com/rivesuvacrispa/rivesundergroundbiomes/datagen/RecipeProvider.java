package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
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
    }

    private static void cook(Consumer<FinishedRecipe> exporter, ItemLike input, Item result, float xp) {
        String group = getItemName(result);
        oreSmelting(exporter, List.of(input), RecipeCategory.MISC, result, xp, 200, group);
        oreBlasting(exporter, List.of(input), RecipeCategory.MISC, result, xp, 100, group);
    }
}
