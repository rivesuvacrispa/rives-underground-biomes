package com.rivesuvacrispa.rivesundergroundbiomes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.rivesuvacrispa.rivesundergroundbiomes.world.ModStructureTags;
import com.rivesuvacrispa.rivesundergroundbiomes.world.ReefLayers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

// removes #not_in_reef structures when they cross reef bounds
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {
    @Shadow
    public abstract Structure getStructure();

    @WrapOperation(
            method = "placeInChunk",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/level/levelgen/structure/StructurePiece;postProcess(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/StructureManager;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/level/levelgen/structure/BoundingBox;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/core/BlockPos;)V"))
    private void rub$skipInReef(
            StructurePiece piece,
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot,
            Operation<Void> original) {
        if (!inReef(level, piece.getBoundingBox(), chunkBox))
            original.call(piece, level, structureManager, generator, random, chunkBox, chunkPos, pivot);
    }

    // decides per chunk at the middle of the piece part in it
    @Unique
    private boolean inReef(WorldGenLevel level, BoundingBox piece, BoundingBox chunkBox) {
        if (!level.registryAccess()
                .registryOrThrow(Registries.STRUCTURE)
                .wrapAsHolder(getStructure())
                .is(ModStructureTags.NOT_IN_REEF)) return false;
        BlockPos middle = new BlockPos(
                (Math.max(piece.minX(), chunkBox.minX()) + Math.min(piece.maxX(), chunkBox.maxX())) / 2,
                (piece.minY() + piece.maxY()) / 2,
                (Math.max(piece.minZ(), chunkBox.minZ()) + Math.min(piece.maxZ(), chunkBox.maxZ())) / 2);
        return ReefLayers.isReef(level.getBiome(middle));
    }
}
