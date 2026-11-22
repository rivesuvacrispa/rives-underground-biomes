package com.rivesuvacrispa.rivesundergroundbiomes.world;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.block.Host;
import com.rivesuvacrispa.rivesundergroundbiomes.block.OreKind;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;

public final class Conversions {
    @SuppressWarnings("unchecked")
    private static final Reference2ObjectOpenHashMap<Block, BlockState>[] BY_ZONE =
            new Reference2ObjectOpenHashMap[Zone.VALUES.length];
    // only inside the zone biome itself, not under its surface biomes
    @SuppressWarnings("unchecked")
    private static final Reference2ObjectOpenHashMap<Block, BlockState>[] BIOME_ONLY =
            new Reference2ObjectOpenHashMap[Zone.VALUES.length];
    // lets the converter skip sections without any
    private static final Set<Block> SOURCES = new ReferenceOpenHashSet<>();

    public static void build() {
        for (Zone zone : Zone.VALUES) {
            Reference2ObjectOpenHashMap<Block, BlockState> map = new Reference2ObjectOpenHashMap<>();
            Host stone = Host.of(zone, false);
            Host deep = Host.of(zone, true);
            map.put(Blocks.STONE, stone.block().defaultBlockState());
            map.put(Blocks.DEEPSLATE, deep.block().defaultBlockState());
            for (OreKind kind : OreKind.VALUES) {
                map.put(kind.stoneOre, ModBlocks.ore(stone, kind).defaultBlockState());
                map.put(kind.deepOre, ModBlocks.ore(deep, kind).defaultBlockState());
            }
            BY_ZONE[zone.ordinal()] = map;
            SOURCES.addAll(map.keySet());
            Reference2ObjectOpenHashMap<Block, BlockState> biomeOnly = new Reference2ObjectOpenHashMap<>();
            if (zone == Zone.ICE) {
                biomeOnly.put(Blocks.WATER, Blocks.ICE.defaultBlockState());
                biomeOnly.put(Blocks.LAVA, Blocks.POWDER_SNOW.defaultBlockState());
            }
            // yeah i guess it is logical to not have water in the desert...
            if (zone == Zone.DESERT) biomeOnly.put(Blocks.WATER, ModBlocks.QUICKSAND.defaultBlockState());
            // replace lava with water because lava would burn the jungle down
            if (zone == Zone.JUNGLE) biomeOnly.put(Blocks.LAVA, Blocks.WATER.defaultBlockState());
            BIOME_ONLY[zone.ordinal()] = biomeOnly;
            SOURCES.addAll(biomeOnly.keySet());
        }
    }

    public static boolean isSource(BlockState state) {
        return SOURCES.contains(state.getBlock());
    }

    // inBiome = the zone biome, not under its surface biome
    // null = the zone leaves the state alone
    public static BlockState convert(Zone zone, boolean inBiome, BlockState state) {
        if (inBiome) {
            BlockState converted = BIOME_ONLY[zone.ordinal()].get(state.getBlock());
            if (converted != null) return converted;
        }
        return BY_ZONE[zone.ordinal()].get(state.getBlock());
    }

    private Conversions() {}
}
