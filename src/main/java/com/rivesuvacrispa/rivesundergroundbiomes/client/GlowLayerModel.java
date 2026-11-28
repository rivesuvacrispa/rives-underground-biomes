package com.rivesuvacrispa.rivesundergroundbiomes.client;

import net.fabricmc.fabric.api.renderer.v1.Renderer;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

class GlowLayerModel extends ForwardingBakedModel {
    private static final Direction[] CULL_FACES = {
        Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null
    };

    private final ResourceLocation glow;
    private final RenderMaterial standard;
    private final RenderMaterial emissive;

    GlowLayerModel(BakedModel model, ResourceLocation glow, Renderer renderer) {
        this.wrapped = model;
        this.glow = glow;
        this.standard = renderer.materialFinder().find();
        this.emissive = renderer.materialFinder()
                .emissive(true)
                .disableDiffuse(true)
                .ambientOcclusion(TriState.FALSE)
                .find();
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(
            BlockAndTintGetter level,
            BlockState state,
            BlockPos pos,
            Supplier<RandomSource> random,
            RenderContext context) {
        emit(state, random.get(), context);
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<RandomSource> random, RenderContext context) {
        emit(null, random.get(), context);
    }

    // quads with the glow texture are drawn at full brightness like emission map in unity's shader graph
    private void emit(@Nullable BlockState state, RandomSource random, RenderContext context) {
        QuadEmitter emitter = context.getEmitter();
        for (Direction cullFace : CULL_FACES) {
            for (BakedQuad quad : wrapped.getQuads(state, cullFace, random)) {
                boolean glowing = quad.getSprite().contents().name().equals(glow);
                emitter.fromVanilla(quad, glowing ? emissive : standard, cullFace)
                        .emit();
            }
        }
    }
}
