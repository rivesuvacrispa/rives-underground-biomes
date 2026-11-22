package com.rivesuvacrispa.rivesundergroundbiomes.block;

import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import com.rivesuvacrispa.rivesundergroundbiomes.world.Zone;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public enum Host {
    SANDSTONE(Zone.DESERT, false, "sandstone", SoundType.STONE),
    SANDSLATE(Zone.DESERT, true, "sandslate", SoundType.DEEPSLATE),
    MUD(Zone.JUNGLE, false, "mud", SoundType.MUD),
    MUDSTONE(Zone.JUNGLE, true, "mudstone", SoundType.MUD_BRICKS),
    PERMAFROST(Zone.ICE, false, "permafrost", SoundType.STONE),
    GLACISLATE(Zone.ICE, true, "glacislate", SoundType.DEEPSLATE);

    public static final Host[] VALUES = values();

    public final Zone zone;
    public final boolean deep;
    public final String id;
    public final SoundType oreSound;

    Host(Zone zone, boolean deep, String id, SoundType oreSound) {
        this.zone = zone;
        this.deep = deep;
        this.id = id;
        this.oreSound = oreSound;
    }

    public Block block() {
        return switch (this) {
            case SANDSTONE -> Blocks.SANDSTONE;
            case MUD -> Blocks.MUD;
            case SANDSLATE -> ModBlocks.SANDSLATE;
            case MUDSTONE -> ModBlocks.MUDSTONE;
            case PERMAFROST -> ModBlocks.PERMAFROST;
            case GLACISLATE -> ModBlocks.GLACISLATE;
        };
    }

    public float hardness() {
        return block().defaultDestroyTime();
    }

    public static Host of(Zone zone, boolean deep) {
        for (Host host : VALUES) {
            if (host.zone == zone && host.deep == deep) return host;
        }
        throw new IllegalArgumentException(zone + " deep=" + deep);
    }
}
