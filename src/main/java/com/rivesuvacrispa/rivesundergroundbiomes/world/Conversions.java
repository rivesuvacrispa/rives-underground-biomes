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
    // features place this where water has to survive the conversion
    // like desert water into quicksand or ice caves water into ice
    // the converter turns it into water after converting the chunk
    public static final BlockState WATER_MARKER = Blocks.STRUCTURE_VOID.defaultBlockState();
    // lets the converter skip sections without any
    private static final Set<Block> SOURCES = new ReferenceOpenHashSet<>();

    public static void build() {
        for (Zone zone : Zone.VALUES) {
            BY_ZONE[zone.ordinal()] = new Reference2ObjectOpenHashMap<>();
            BIOME_ONLY[zone.ordinal()] = new Reference2ObjectOpenHashMap<>();
        }
        for (Host host : Host.VALUES) {
            if (host.zone == Zone.REEF) continue;
            Reference2ObjectOpenHashMap<Block, BlockState> map = BY_ZONE[host.zone.ordinal()];
            map.put(host.deep ? Blocks.DEEPSLATE : Blocks.STONE, host.block().defaultBlockState());
            for (OreKind kind : OreKind.VALUES) {
                map.put(kind.vanillaOre(host.deep), ModBlocks.ore(host, kind).defaultBlockState());
            }
        }
        Reference2ObjectOpenHashMap<Block, BlockState> roof = BY_ZONE[Zone.REEF.ordinal()];
        Reference2ObjectOpenHashMap<Block, BlockState> reef = BIOME_ONLY[Zone.REEF.ordinal()];
        roof.put(Blocks.STONE, Blocks.SANDSTONE.defaultBlockState());
        reef.put(Blocks.STONE, ModBlocks.PEARLSTONE.defaultBlockState());
        reef.put(Blocks.DEEPSLATE, ModBlocks.PEARLSTONE.defaultBlockState());
        for (OreKind kind : OreKind.VALUES) {
            roof.put(kind.stoneOre, ModBlocks.ore(Host.SANDSTONE, kind).defaultBlockState());
            BlockState pearlstoneOre = ModBlocks.ore(Host.PEARLSTONE, kind).defaultBlockState();
            reef.put(kind.stoneOre, pearlstoneOre);
            reef.put(kind.deepOre, pearlstoneOre);
        }
        for (Zone zone : Zone.VALUES) {
            Reference2ObjectOpenHashMap<Block, BlockState> biomeOnly = BIOME_ONLY[zone.ordinal()];
            if (zone == Zone.ICE) {
                biomeOnly.put(Blocks.WATER, Blocks.ICE.defaultBlockState());
                biomeOnly.put(Blocks.LAVA, Blocks.POWDER_SNOW.defaultBlockState());
            }
            // yeah i guess it is logical to not have water in the desert...
            if (zone == Zone.DESERT) biomeOnly.put(Blocks.WATER, ModBlocks.QUICKSAND.defaultBlockState());
            // replace lava with water because lava would burn the jungle down
            if (zone == Zone.JUNGLE) biomeOnly.put(Blocks.LAVA, Blocks.WATER.defaultBlockState());
            SOURCES.addAll(BY_ZONE[zone.ordinal()].keySet());
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
