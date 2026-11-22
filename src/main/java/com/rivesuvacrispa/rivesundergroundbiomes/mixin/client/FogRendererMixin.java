package com.rivesuvacrispa.rivesundergroundbiomes.mixin.client;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.rivesuvacrispa.rivesundergroundbiomes.ModBlocks;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// thick sand colored fog with the camera inside quicksand
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Unique
    private static final int QUICKSAND_FOG = 0xA8916A;

    @Shadow
    private static float fogRed;

    @Shadow
    private static float fogGreen;

    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void rub$quicksandFogColor(
            Camera camera,
            float partialTick,
            ClientLevel level,
            int renderDistance,
            float darkenAmount,
            CallbackInfo ci) {
        if (!rub$inQuicksand(camera)) return;
        fogRed = (QUICKSAND_FOG >> 16 & 255) / 255F;
        fogGreen = (QUICKSAND_FOG >> 8 & 255) / 255F;
        fogBlue = (QUICKSAND_FOG & 255) / 255F;
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0.0F);
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    private static void rub$quicksandFog(
            Camera camera,
            FogRenderer.FogMode mode,
            float farPlane,
            boolean thickFog,
            float partialTick,
            CallbackInfo ci) {
        if (!rub$inQuicksand(camera)) return;
        RenderSystem.setShaderFogStart(0.0F);
        RenderSystem.setShaderFogEnd(3.0F);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);
        ci.cancel();
    }

    @Unique
    private static boolean rub$inQuicksand(Camera camera) {
        Level level = camera.getEntity() == null ? null : camera.getEntity().level();
        return level != null && level.getBlockState(camera.getBlockPosition()).is(ModBlocks.QUICKSAND);
    }
}
