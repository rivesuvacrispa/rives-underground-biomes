package com.rivesuvacrispa.rivesundergroundbiomes.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// numbers copied from the Create 6 vanilla ore recipes
// the 12.5% cobblestone by product becomes the ore host block
public class CreateCrushingProvider implements DataProvider {
    private record Yield(int count, float extraChance, int xp, int time) {}

    private final PackOutput.PathProvider paths;

    public CreateCrushingProvider(FabricDataOutput output) {
        this.paths = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipes");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (Host host : Host.VALUES) {
            for (OreKind kind : OreKind.VALUES) {
                Yield yield = yieldOf(kind, host.deep);
                String product = product(kind);
                JsonArray results = new JsonArray();
                results.add(result(product, yield.count, 1F));
                results.add(result(product, 1, yield.extraChance));
                results.add(result("create:experience_nugget", yield.xp, 0.75F));
                results.add(result(id(host.block()), 1, 0.125F));
                futures.add(save(writer, ModBlocks.ore(host, kind), yield.time, results));
            }
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private static Yield yieldOf(OreKind kind, boolean deep) {
        return switch (kind) {
            case COAL -> deep ? new Yield(2, 0.25F, 1, 300) : new Yield(1, 0.75F, 1, 150);
            case IRON -> deep ? new Yield(2, 0.25F, 1, 350) : new Yield(1, 0.75F, 1, 250);
            case COPPER -> deep ? new Yield(7, 0.25F, 1, 350) : new Yield(5, 0.25F, 1, 250);
            case GOLD -> deep ? new Yield(2, 0.25F, 2, 350) : new Yield(1, 0.75F, 2, 250);
            case REDSTONE -> deep ? new Yield(7, 0.5F, 1, 350) : new Yield(6, 0.5F, 1, 250);
            case LAPIS -> deep ? new Yield(12, 0.5F, 1, 350) : new Yield(10, 0.5F, 1, 250);
            case DIAMOND, EMERALD -> deep ? new Yield(2, 0.25F, 1, 450) : new Yield(1, 0.75F, 1, 350);
        };
    }

    private static String product(OreKind kind) {
        return switch (kind) {
            case IRON -> "create:crushed_raw_iron";
            case COPPER -> "create:crushed_raw_copper";
            case GOLD -> "create:crushed_raw_gold";
            default -> id(kind.smeltResult);
        };
    }

    private CompletableFuture<?> save(CachedOutput writer, ItemLike input, int time, JsonArray results) {
        JsonObject recipe = new JsonObject();
        JsonArray conditions = new JsonArray();
        JsonObject createLoaded = new JsonObject();
        createLoaded.addProperty("condition", "fabric:all_mods_loaded");
        JsonArray mods = new JsonArray();
        mods.add("create");
        createLoaded.add("values", mods);
        conditions.add(createLoaded);
        recipe.add("fabric:load_conditions", conditions);
        recipe.addProperty("type", "create:crushing");
        JsonArray ingredients = new JsonArray();
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", id(input));
        ingredients.add(ingredient);
        recipe.add("ingredients", ingredients);
        recipe.addProperty("processingTime", time);
        recipe.add("results", results);
        String name = BuiltInRegistries.ITEM.getKey(input.asItem()).getPath();
        return DataProvider.saveStable(writer, recipe, paths.json(RivesUndergroundBiomes.id("crushing/" + name)));
    }

    private static JsonObject result(String item, int count, float chance) {
        JsonObject result = new JsonObject();
        if (chance < 1F) result.addProperty("chance", chance);
        if (count > 1) result.addProperty("count", count);
        result.addProperty("item", item);
        return result;
    }

    private static String id(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).toString();
    }

    @Override
    public String getName() {
        return "Create crushing";
    }
}
