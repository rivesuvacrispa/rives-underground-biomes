package com.rivesuvacrispa.rivesundergroundbiomes.mixin.client;

import com.rivesuvacrispa.rivesundergroundbiomes.RivesUndergroundBiomes;
import com.rivesuvacrispa.rivesundergroundbiomes.block.BiomeChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(Sheets.class)
public abstract class SheetsMixin {
    @Unique
    private static final Map<Block, Material> rub$chestMaterials = new ConcurrentHashMap<>();

    @Inject(
            method =
                    "chooseMaterial(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/level/block/state/properties/ChestType;Z)Lnet/minecraft/client/resources/model/Material;",
            at = @At("HEAD"),
            cancellable = true)
    private static void rub$biomeChest(
            BlockEntity blockEntity, ChestType type, boolean holiday, CallbackInfoReturnable<Material> cir) {
        if (!(blockEntity instanceof BiomeChestBlockEntity)) return;
        cir.setReturnValue(rub$chestMaterials.computeIfAbsent(
                blockEntity.getBlockState().getBlock(),
                block -> new Material(
                        Sheets.CHEST_SHEET,
                        RivesUndergroundBiomes.id("entity/"
                                + BuiltInRegistries.BLOCK.getKey(block).getPath()))));
    }
}
